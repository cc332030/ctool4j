package com.c332030.ctool4j.cache.aop;

import com.c332030.ctool4j.cache.annotation.CCacheId;
import com.c332030.ctool4j.cache.annotation.CCacheable;
import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.classes.CClassUtils;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.util.CArrUtils;
import com.c332030.ctool4j.spring.util.CAspectUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
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
public class CCacheAspect {

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
    private static final CClassValue<MethodHandle> CACHE_ID_HANDLE_CLASS_VALUE = CClassValue.of(type -> {
        val cacheIdField = CReflectUtils.getAllFieldMap(type)
            .values()
            .stream()
            .filter(field -> field.isAnnotationPresent(CCacheId.class))
            .findFirst()
            .orElse(null);
        if (null == cacheIdField) {
            return null;
        }
        return unreflectGetter(cacheIdField);
    });

    @SneakyThrows
    private static MethodHandle unreflectGetter(Field field) {
        field.setAccessible(true);
        return MethodHandles.lookup().unreflectGetter(field);
    }

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
                if(log.isDebugEnabled()) {
                    log.debug("启用本地缓存");
                }
                return getLocalCache(joinPoint, cacheable);
            }
        } catch (Exception e) {
            log.error("获取缓存失败，cacheable: {}", cacheable);
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
     */
    private Cache<String, Object> getCache(Class<?> namespace, int expire) {
        val expireCaches = NAMESPACE_CACHES.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>());
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
        if(log.isDebugEnabled()) {
            log.debug("namespace: {}", namespace);
        }

        val expire = cacheable.expire();
        if(log.isDebugEnabled()) {
            log.debug("expire: {}", expire);
        }

        val cache = getCache(namespace, expire);

        val args = joinPoint.getArgs();
        val argOne = CArrUtils.get(args, 0);

        // TODO 无方法参数缓存
        if (null != argOne) {

            val cacheKey = getCacheKey(argOne, cacheable);
            if(log.isDebugEnabled()) {
                log.debug("cacheKey: {}", cacheKey);
            }

            val cacheValue = cache.getIfPresent(cacheKey);
            if(null != cacheValue) {
                if(log.isDebugEnabled()) {
                    log.debug("命中缓存：{}", cacheValue);
                }
                return cacheValue;
            }

            if(log.isInfoEnabled()) {
                log.info("没有缓存或已过期，cacheKey: {}", cacheKey);
            }

            val valueNew = CAspectUtils.process(joinPoint);
            if(null != valueNew) {
                cache.put(cacheKey, valueNew);

                if(log.isInfoEnabled()) {
                    log.info("新值 cacheValue： {}", valueNew);
                }
            }

            return valueNew;
        }

        if(log.isDebugEnabled()) {
            log.debug("方法无参数，跳过缓存");
        }
        return CAspectUtils.process(joinPoint);
    }

}
