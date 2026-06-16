package com.c332030.ctool4j.cache.aop;

import com.c332030.ctool4j.cache.annotation.CCacheId;
import com.c332030.ctool4j.cache.annotation.CCacheable;
import com.c332030.ctool4j.cache.service.CCacheService;
import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.classes.CClassUtils;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.util.CArrUtils;
import com.c332030.ctool4j.spring.util.CAspectUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Description: CCacheAspect
 * </p>
 *
 * @since 2025/9/27
 */
@CustomLog
@Aspect
@Component
@AllArgsConstructor
public class CCacheAspect {

    CCacheService cacheService;

    /**
     * 每个 namespace 下允许的最大不同过期时间 Cache 实例数，防止无界增长
     */
    private static final int MAX_EXPIRE_CACHES_PER_NAMESPACE = 16;

    /**
     * 缓存 key: namespace Class, value: (expire -> Cache)
     * 每个 namespace 下按不同过期时间分别维护一个 Guava Cache
     */
    private static final ConcurrentHashMap<Class<?>, ConcurrentHashMap<Integer, Cache<String, Object>>> NAMESPACE_CACHES
        = new ConcurrentHashMap<>();

    private static final CClassValue<ICCacheIdConverter<Object, Object>> CLASS_ID_CONVERTER = CClassValue
        .of(e -> CObjUtils.anyType(CReflectUtils.newInstance(e)));

    /**
     * 缓存 @CCacheId 字段的 MethodHandle，替代反射 Field.get，性能提升约 3-5 倍
     */
    private static final CClassValue<MethodHandle> CACHE_ID_HANDLE_CLASS_VALUE = CClassValue
        .of(type -> CReflectUtils.getAllFieldMap(type)
            .values()
            .stream()
            .filter(field -> field.isAnnotationPresent(CCacheId.class))
            .findFirst()
            .map(CReflectUtils::getMethodHandle)
            .orElse(null)
        );

    /**
     * 缓存切面
     * @param joinPoint 切入点
     * @return 方法执行结果
     */
    @Around("@annotation(com.c332030.ctool4j.cache.annotation.CCacheable)")
    public Object cacheAspect(ProceedingJoinPoint joinPoint) {

        val method = CAspectUtils.getMethod(joinPoint);
        val cacheable = CReflectUtils.getAnnotationCached(method, CCacheable.class);
        try {
            if (cacheable.local()) {
                log.debug("启用本地缓存");
                return getLocalCache(joinPoint, cacheable);
            } else {
                log.debug("启用 Redis 缓存");
                return getRedisCache(joinPoint, method, cacheable);
            }
        } catch (Exception e) {
            log.error("获取缓存失败，cacheable: {}", cacheable, e);
        }

        return CAspectUtils.process(joinPoint);
    }

    /**
     * 生成缓存 key
     * @param object 方法参数
     * @param cacheable 缓存注解
     * @return 缓存 key
     */
    @SneakyThrows
    public String getCacheKey(
        Object object,
        CCacheable cacheable
    ) {

        val idConverter = CLASS_ID_CONVERTER.get(cacheable.idConverter());

        val objClass = object.getClass();
        val cacheId = CClassUtils.isJdkClass(objClass)
            ? null
            : getCacheIdByHandle(object, objClass);

        return idConverter.apply(cacheId, object);
    }

    /**
     * 通过 MethodHandle 获取 @CCacheId 标注的字段值
     */
    @SneakyThrows
    private Object getCacheIdByHandle(Object object, Class<?> objClass) {
        val handle = CACHE_ID_HANDLE_CLASS_VALUE.get(objClass);
        if (null == handle) {
            return null;
        }
        return handle.invoke(object);
    }

    /**
     * 获取或创建 namespace 下指定过期时间的 Guava Cache
     * <p>
     * 防御：每个 namespace 最多创建 {@value #MAX_EXPIRE_CACHES_PER_NAMESPACE} 个不同 expire 的 Cache 实例，
     * 超过阈值时复用已有的最长过期时间 Cache，防止无界增长。
     */
    private Cache<String, Object> getCache(Class<?> namespace, int expire) {

        val expireCaches = NAMESPACE_CACHES.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>());

        // 已存在直接返回
        val existing = expireCaches.get(expire);
        if (null != existing) {
            return existing;
        }

        // 防御：超过上限时复用已有 cache，避免无界增长
        if (expireCaches.size() >= MAX_EXPIRE_CACHES_PER_NAMESPACE) {
            if (log.isWarnEnabled()) {
                log.warn("namespace [{}] 下 expire cache 数量已达上限 {}，复用已有 cache",
                    namespace.getSimpleName(), MAX_EXPIRE_CACHES_PER_NAMESPACE);
            }
            return expireCaches.values().iterator().next();
        }

        return expireCaches.computeIfAbsent(expire, e -> {
            val builder = CacheBuilder.newBuilder();
            if (e > 0) {
                builder.expireAfterWrite(e, TimeUnit.SECONDS);
            }
            return builder.build();
        });
    }

    /**
     * 获取本地缓存
     * @param joinPoint 切入点
     * @param cacheable 缓存注解
     * @return 本地缓存或执行结果
     */
    public Object getLocalCache(
        ProceedingJoinPoint joinPoint,
        CCacheable cacheable
    ) {

        val namespace = cacheable.namespace();
        if (log.isDebugEnabled()) {
            log.debug("namespace: {}", namespace);
        }

        val expire = cacheable.expire();
        val cache = getCache(namespace, expire);

        val args = joinPoint.getArgs();
        val argOne = CArrUtils.get(args, 0);

        // TODO 无方法参数缓存
        if (null != argOne) {

            val cacheKey = getCacheKey(argOne, cacheable);
            if (log.isDebugEnabled()) {
                log.debug("cacheKey: {}, expire: {}", cacheKey, expire);
            }

            val cacheValue = cache.getIfPresent(cacheKey);
            if (null != cacheValue) {
                if (log.isDebugEnabled()) {
                    log.debug("命中缓存，cacheValue：{}", cacheValue);
                }
                return cacheValue;
            }

            log.info("没有缓存或已过期，cacheKey: {}", cacheKey);

            val valueNew = CAspectUtils.process(joinPoint);
            if (null != valueNew) {
                cache.put(cacheKey, valueNew);
                log.info("新值 cacheValue： {}", valueNew);
            }

            return valueNew;
        }

        if (log.isDebugEnabled()) {
            log.debug("方法无参数，跳过本地缓存");
        }
        return CAspectUtils.process(joinPoint);
    }

    /**
     * 获取 Redis 缓存
     * <p>
     * 缓存 key 格式：namespace:cacheKey（由 getCacheKey 生成）
     * @param joinPoint 切入点
     * @param cacheable 缓存注解
     * @param method 目标方法（用于获取返回类型做反序列化）
     * @return Redis 缓存或执行结果
     */
    private Object getRedisCache(
        ProceedingJoinPoint joinPoint,
        Method method,
        CCacheable cacheable
    ) {

        val namespace = cacheable.namespace();
        if (log.isDebugEnabled()) {
            log.debug("Redis namespace: {}", namespace.getSimpleName());
        }

        val expire = cacheable.expire();

        val args = joinPoint.getArgs();
        val argOne = CArrUtils.get(args, 0);

        if (null != argOne) {

            val cacheKey = getCacheKey(argOne, cacheable);
            val redisKey = namespace.getSimpleName() + ":" + cacheKey;
            if (log.isDebugEnabled()) {
                log.debug("Redis cacheKey: {}, expire: {}", redisKey, expire);
            }

            val returnType = method.getReturnType();
            return cacheService.getCache(
                redisKey, CObjUtils.anyType(returnType),
                expire,
                () -> CAspectUtils.process(joinPoint)
            );
        }

        if (log.isDebugEnabled()) {
            log.debug("方法无参数，跳过 Redis 缓存");
        }
        return CAspectUtils.process(joinPoint);
    }

}
