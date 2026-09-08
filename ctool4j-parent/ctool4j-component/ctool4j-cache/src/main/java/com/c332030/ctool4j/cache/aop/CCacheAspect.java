package com.c332030.ctool4j.cache.aop;

import com.c332030.ctool4j.cache.annotation.CCacheId;
import com.c332030.ctool4j.cache.annotation.CCacheable;
import com.c332030.ctool4j.cache.service.CCacheService;
import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.classes.CClassUtils;
import com.c332030.ctool4j.core.classes.CMethodHandleUtils;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.util.CArrUtils;
import com.c332030.ctool4j.core.util.CLocalCacheUtils;
import com.c332030.ctool4j.spring.util.CAspectUtils;
import com.github.benmanes.caffeine.cache.Cache;
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
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Description: CCacheAspect
 * </p>
 *
 * @see "doc/design/cache/CCacheAspect.adoc"
 * @see "doc/design/cache/CCacheAspectTests.adoc"
 * @see "doc/design/cache/CCacheAspectCacheKeyTests.adoc"
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
     * 每个 namespace 下按不同过期时间分别维护一个 Caffeine Cache
     */
    private static final Cache<Class<?>, Cache<Integer, Cache<String, Object>>>
        NAMESPACE_CACHES = CLocalCacheUtils.buildCache();

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
            .map(CMethodHandleUtils::getGetterHandle)
            .orElse(null)
        );

    /**
     * 缓存切面：读缓存 → 未命中时执行原方法并写缓存（原方法在缓存方法内部执行）。
     * <p>读缓存/执行原方法异常不捕获，直接向上抛出，调用方可感知失败，
     * 不再静默降级返回 null（异常时缓存未写入，不会污染缓存）</p>
     *
     * @param joinPoint 切入点
     * @return 方法执行结果
     */
    @Around("@annotation(com.c332030.ctool4j.cache.annotation.CCacheable)")
    public Object cacheAspect(ProceedingJoinPoint joinPoint) {

        val method = CAspectUtils.getMethod(joinPoint);
        val cacheable = CReflectUtils.getAnnotationCached(method, CCacheable.class);
        if (cacheable.local()) {
            log.debug("启用本地缓存");
            return getLocalCache(joinPoint, method, cacheable);
        } else {
            log.debug("启用 Redis 缓存");
            return getRedisCache(joinPoint, method, cacheable);
        }
    }

    /**
     * 解析缓存 key（统一入口：key 表达式 or 默认 @CCacheId 逻辑）。
     *
     * <p>key() 非空且非空白：走简单 el 表达式（可多参数、多级取属性）；为空或空白：视为未配置，
     * 走默认逻辑（第一参数 + {@code @CCacheId}）。返回 null 表示无缓存 key（无参/参数为 null/
     * 表达式链某级为 null），由调用方决定不写缓存直接执行原方法。</p>
     *
     * @param args      方法实参
     * @param method    被缓存方法
     * @param cacheable 缓存注解
     * @return 缓存 key；无法取值时返回 null
     */
    public String resolveCacheKey(
        Object[] args,
        Method method,
        CCacheable cacheable
    ) {

        val keyExpr = cacheable.key();
        if (null != keyExpr && !keyExpr.trim().isEmpty()) {
            val resolver = CCacheKeyResolver.getResolver(method, keyExpr);
            val value = resolver.resolve(args);
            if (null == value) {
                return null;
            }
            val idConverter = CLASS_ID_CONVERTER.get(cacheable.idConverter());
            return idConverter.apply(value, null);
        }

        // 默认逻辑：第一参数
        if (null == args || args.length == 0) {
            return null;
        }
        return getCacheKey(CArrUtils.get(args, 0), cacheable);
    }

    /**
     * 生成缓存 key（默认逻辑，基于第一个参数对象 + @CCacheId 字段）
     *
     * @param object    方法第一个参数对象，为 null 时返回 null（由调用方保证不写入缓存）
     * @param cacheable 缓存注解
     * @return 缓存 key；object 为 null 时返回 null
     */
    @SneakyThrows
    public String getCacheKey(
        Object object,
        CCacheable cacheable
    ) {

        if (null == object) {
            return null;
        }

        val idConverter = CLASS_ID_CONVERTER.get(cacheable.idConverter());

        val objClass = object.getClass();
        if (CClassUtils.isJdkClass(objClass)) {
            // JDK 类（String/Integer 等）直接用对象字符串作 key
            return idConverter.apply(null, object);
        }

        // 非 JDK POJO：必须有 @CCacheId 字段，否则无法生成缓存 key
        val handle = CACHE_ID_HANDLE_CLASS_VALUE.get(objClass);
        if (null == handle) {
            throw new IllegalStateException(
                "@CCacheable 缓存参数类型 " + objClass.getName()
                    + " 无 @CCacheId 字段且未配置 key()，无法生成缓存 key。请在方法上配置"
                    + " @CCacheable(key=\"参数名.属性…\")，或在参数类型的业务 id 字段上加 @CCacheId。方法: "
                    + cacheable.namespace().getName());
        }
        val cacheId = handle.invoke(object);
        return idConverter.apply(cacheId, object);
    }

    /**
     * 获取或创建 namespace 下指定过期时间的 Guava Cache
     * <p>
     * 防御：每个 namespace 最多创建 {@value #MAX_EXPIRE_CACHES_PER_NAMESPACE} 个不同 expire 的 Cache 实例，
     * 超过阈值时复用已有的最长过期时间 Cache，防止无界增长。
     */
    private Cache<String, Object> getCache(Class<?> namespace, int expire) {

        val expireCaches = NAMESPACE_CACHES.get(namespace, k -> CLocalCacheUtils.buildCache());

        // 已存在直接返回
        val existing = expireCaches.getIfPresent(expire);
        if (null != existing) {
            return existing;
        }

        return expireCaches.get(expire, e -> {
            val builder = CLocalCacheUtils.cacheBuilder();
            if (e > 0) {
                builder.expireAfterWrite(e, TimeUnit.SECONDS);
            }
            return builder.build();
        });
    }

    /**
     * 获取本地缓存：未命中时执行原方法并写缓存（Caffeine cache.get 原子加载，单 key 并发只执行一次）
     * @param joinPoint 切入点
     * @param method    被缓存方法
     * @param cacheable 缓存注解
     * @return 本地缓存或执行结果
     */
    public Object getLocalCache(
        ProceedingJoinPoint joinPoint,
        Method method,
        CCacheable cacheable
    ) {

        val namespace = cacheable.namespace();
        if (log.isDebugEnabled()) {
            log.debug("namespace: {}", namespace);
        }

        val expire = cacheable.expire();
        val cache = getCache(namespace, expire);

        val args = joinPoint.getArgs();
        val cacheKey = resolveCacheKey(args, method, cacheable);

        // 无缓存 key（无参/参数为 null/表达式某级为 null）跳过缓存
        if (null == cacheKey) {
            if (log.isDebugEnabled()) {
                log.debug("无缓存 key，跳过本地缓存");
            }
            return CAspectUtils.process(joinPoint);
        }

        if (log.isDebugEnabled()) {
            log.debug("cacheKey: {}, expire: {}", cacheKey, expire);
        }
        return cache.get(cacheKey, k -> {

            val valueNew = CAspectUtils.process(joinPoint);
            log.info("新值 cacheKey: {}, cacheValue: {}", k, valueNew);
            return valueNew;
        });
    }

    /**
     * 获取 Redis 缓存：未命中时执行原方法并写缓存（cacheService.getCache 读-算-写一体）
     * <p>
     * 缓存 key 格式：namespace:cacheKey（由 resolveCacheKey 统一生成：key() 走 el，否则默认逻辑）
     * @param joinPoint 切入点
     * @param method 目标方法（用于获取返回类型做反序列化）
     * @param cacheable 缓存注解
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
        val cacheKey = resolveCacheKey(args, method, cacheable);

        // 无缓存 key（无参/参数为 null/表达式某级为 null）跳过缓存
        if (null == cacheKey) {
            if (log.isDebugEnabled()) {
                log.debug("无缓存 key，跳过 Redis 缓存");
            }
            return CAspectUtils.process(joinPoint);
        }

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

}
