package com.c332030.ctool4j.cache.aop;

import com.c332030.ctool4j.cache.annotation.CCacheRemove;
import com.c332030.ctool4j.cache.annotation.CCacheId;
import com.c332030.ctool4j.cache.annotation.CCacheUpdate;
import com.c332030.ctool4j.cache.annotation.CCacheable;
import com.c332030.ctool4j.cache.service.CCacheService;
import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.classes.CClassUtils;
import com.c332030.ctool4j.core.classes.CElKeyResolveUtils;
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
 * <p>{@code @Aspect} + {@code @Component} 切面，拦截标注 {@code @CCacheable} / {@code @CCacheRemove} /
 * {@code @CCacheUpdate} 的方法，统一管理本地（Caffeine）与 Redis 两级缓存。</p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>key 解析：{@link #resolveCacheKey(Object[], Method, CCacheable)} 与其独立属性重载；
 *   默认逻辑 {@link #getCacheKey(Object, CCacheable)} 及其重载。</li>
 *   <li>读路径：{@link #getLocalCache}（本地）、{@link #getRedisCache}（Redis）。</li>
 *   <li>写路径：{@link #removeCache} / {@link #updateCache} 按 {@code local} 分流到本地与 Redis 实现。</li>
 * </ul>
 *
 * <h2>设计思路总述</h2>
 * <ul>
 *   <li>三个注解共用一套 key 规则：{@code key()} 非空白走简单 el 表达式（{@code CElKeyResolveUtils}，
 *   支持多参数与多级取属性），否则走默认逻辑（第一个参数 + {@code @CCacheId} 字段 + idConverter）。</li>
 *   <li>读写语义一致：写路径先执行原方法，成功（未抛异常）后才改缓存；异常向上抛出且不写/不删缓存，
 *   保证缓存与数据源一致；null 值不写缓存，与 Redis 路径 null 语义对齐。</li>
 *   <li>性能：{@code @CCacheId} 字段经 MethodHandle 读取（按类缓存，替代反射）；本地缓存按
 *   namespace + expire 分组并设实例数上限，防无界增长。</li>
 * </ul>
 * <p>详细设计、详细步骤、兜底与已知限制见各方法 javadoc。</p>
 *
 * @since 2025/9/27
 * @version 1.0
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
     *
     * <p><b>详细设计</b>：{@code @Around} 拦截 {@code @CCacheable}，按 {@code local()} 分流——
     * {@code true} 走本地 Caffeine 缓存（{@link #getLocalCache}），
     * {@code false} 走 Redis 缓存（{@link #getRedisCache}）。</p>
     *
     * <p><b>详细步骤</b>：取被拦截方法 → 读取方法上的 {@code @CCacheable}（经
     * {@code CReflectUtils.getAnnotationCached} 缓存读取）→ {@code local()} 为 true 记 debug 日志并走本地缓存，
     * 否则记 debug 日志并走 Redis 缓存 → 返回结果。</p>
     *
     * <p><b>异常与兜底</b>：读缓存/执行原方法异常不捕获，直接向上抛出，调用方可感知失败，
     * 不做静默降级返回 null（异常时缓存未写入，不会污染缓存）。</p>
     * <ul>
     *   <li>{@link #cacheAspect(ProceedingJoinPoint)}：{@code @CCacheable} 读缓存、未命中执行原方法并写缓存。</li>
     * </ul>
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
     * 缓存删除切面：执行原方法 → 成功（未抛异常）后删除对应缓存。
     *
     * <p><b>详细步骤</b>：取方法与 {@code @CCacheRemove} 注解 → <b>先执行原方法</b>
     * （{@code CAspectUtils.process}）→ 原方法正常返回后调用 {@link #removeCache} 删除缓存 → 返回原方法结果。</p>
     *
     * <p><b>异常与兜底</b>：原方法执行异常时向上抛出，<b>不删除缓存</b>（保持缓存与数据源一致，避免误删）。</p>
     * <ul>
     *   <li>{@link #cacheRemoveAspect(ProceedingJoinPoint)}：{@code @CCacheRemove} 方法成功后删除缓存。</li>
     * </ul>
     *
     * @param joinPoint 切入点
     * @return 方法执行结果
     */
    @Around("@annotation(com.c332030.ctool4j.cache.annotation.CCacheRemove)")
    public Object cacheRemoveAspect(ProceedingJoinPoint joinPoint) {

        val method = CAspectUtils.getMethod(joinPoint);
        val remove = CReflectUtils.getAnnotationCached(method, CCacheRemove.class);

        // 先执行原方法，成功（不抛异常）后才删除缓存
        val result = CAspectUtils.process(joinPoint);

        removeCache(joinPoint, method, remove);
        return result;
    }

    /**
     * 缓存更新切面：执行原方法 → 成功（未抛异常）后用返回值更新缓存。
     *
     * <p><b>详细步骤</b>：取方法与 {@code @CCacheUpdate} 注解 → <b>先执行原方法</b>
     * （{@code CAspectUtils.process}）→ 原方法正常返回后以返回值调用 {@link #updateCache} 更新缓存 →
     * 返回原方法结果。</p>
     *
     * <p><b>异常与兜底</b>：原方法执行异常时向上抛出，<b>不写入缓存</b>（避免把失败结果写入缓存）；
     * 返回值为 null 时由 {@link #updateCache} 跳过写入。</p>
     * <ul>
     *   <li>{@link #cacheUpdateAspect(ProceedingJoinPoint)}：{@code @CCacheUpdate} 方法成功后用返回值更新缓存。</li>
     * </ul>
     *
     * @param joinPoint 切入点
     * @return 方法执行结果
     */
    @Around("@annotation(com.c332030.ctool4j.cache.annotation.CCacheUpdate)")
    public Object cacheUpdateAspect(ProceedingJoinPoint joinPoint) {

        val method = CAspectUtils.getMethod(joinPoint);
        val update = CReflectUtils.getAnnotationCached(method, CCacheUpdate.class);

        // 先执行原方法，成功（不抛异常）后才更新缓存
        val result = CAspectUtils.process(joinPoint);

        updateCache(joinPoint, method, update, result);
        return result;
    }

    /**
     * 解析缓存 key（统一入口：key 表达式 or 默认 @CCacheId 逻辑）。
     *
     * <p><b>详细设计</b>：本方法仅为 {@code @CCacheable} 的属性重载，直接委托给独立属性重载，
     * 使三个注解共用同一套 key 规则。</p>
     *
     * <p><b>返回 null 的含义</b>：无法取得缓存 key（无参/参数为 null/表达式链某级为 null），
     * 调用方据此跳过缓存、直接执行原方法。</p>
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
        return resolveCacheKey(args, method, cacheable.namespace(), cacheable.key(), cacheable.idConverter());
    }

    /**
     * 解析缓存 key（独立属性重载，供 {@link CCacheRemove} / {@link CCacheUpdate} 复用同一套 key 规则）。
     *
     * <p><b>详细步骤</b>：</p>
     * <ol>
     *   <li>{@code keyExpr} 非空且非空白 → 走简单 el 表达式：由
     *   {@code CElKeyResolveUtils.getResolver(method, keyExpr)} 取得解析器（首次使用时解析并校验表达式、
     *   按方法缓存），{@code resolve(args)} 求值；求值结果为 null → 返回 null；否则用 {@code idConverter}
     *   转换（<b>此时 object 参数传 null</b>，即表达式结果仅作 key 输入，不回传原始参数对象）；</li>
     *   <li>{@code keyExpr} 为空或空白 → 走默认逻辑：方法无参（{@code args} 为 null 或长度 0）返回 null；
     *   否则取第一个参数调用 {@link #getCacheKey(Object, Class, Class)}。</li>
     * </ol>
     *
     * <p><b>已知限制</b>：el 表达式按参数名取值依赖编译期保留形参名（消费方需开启 {@code -parameters}）；
     * 属性链循环引用仅运行期可检出。为 key 唯一性，建议取业务属性而非整个 POJO。</p>
     *
     * @param args          方法实参
     * @param method        目标方法
     * @param namespace     缓存命名空间类
     * @param keyExpr       key 表达式（为空走默认 @CCacheId 逻辑）
     * @param idConverterClass 缓存 id 生成类
     * @return 缓存 key；无法取值时返回 null
     */
    public String resolveCacheKey(
        Object[] args,
        Method method,
        Class<?> namespace,
        String keyExpr,
        Class<? extends ICCacheIdConverter<?, ?>> idConverterClass
    ) {

        if (null != keyExpr && !keyExpr.trim().isEmpty()) {
            val resolver = CElKeyResolveUtils.getResolver(method, keyExpr);
            val value = resolver.resolve(args);
            if (null == value) {
                return null;
            }
            val idConverter = CLASS_ID_CONVERTER.get(idConverterClass);
            return idConverter.apply(value, null);
        }

        // 默认逻辑：第一参数
        if (null == args || args.length == 0) {
            return null;
        }
        return getCacheKey(CArrUtils.get(args, 0), namespace, idConverterClass);
    }

    /**
     * 生成缓存 key（默认逻辑，基于第一个参数对象 + @CCacheId 字段）
     *
     * <p><b>详细设计</b>：{@code @CCacheable} 的属性重载，委托给独立属性重载，语义与其一致。</p>
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
        return getCacheKey(object, cacheable.namespace(), cacheable.idConverter());
    }

    /**
     * 生成缓存 key（独立属性重载，供 {@link CCacheRemove} / {@link CCacheUpdate} 复用同一套 key 规则）。
     *
     * <p><b>详细步骤</b>：</p>
     * <ol>
     *   <li>{@code object} 为 null → 返回 null（调用方据此跳过缓存）；</li>
     *   <li>取 idConverter 实例（按类缓存于 {@code CLASS_ID_CONVERTER}，反射实例化后经
     *   {@code CObjUtils.anyType} 适配）；</li>
     *   <li>参数为 JDK 类（String/Integer 等）→ 直接用对象字符串作 key
     *   （{@code idConverter.apply(null, object)}）；</li>
     *   <li>非 JDK POJO → 经 {@code CACHE_ID_HANDLE_CLASS_VALUE}（按类缓存的 MethodHandle，
     *   替代反射、性能提升约 3-5 倍）读 {@code @CCacheId} 字段值，再交 idConverter；
     *   无 {@code @CCacheId} 字段且未配 {@code key()} 时抛 {@link IllegalStateException}
     *   （提示配置 key() 或在 id 字段加 @CCacheId，避免生成歧义 key）。</li>
     * </ol>
     *
     * @param object           方法第一个参数对象，为 null 时返回 null
     * @param namespace        缓存命名空间类
     * @param idConverterClass 缓存 id 生成类
     * @return 缓存 key；object 为 null 时返回 null
     * @throws IllegalStateException POJO 参数无 {@code @CCacheId} 字段且未配置 {@code key()} 时抛出
     */
    @SneakyThrows
    public String getCacheKey(
        Object object,
        Class<?> namespace,
        Class<? extends ICCacheIdConverter<?, ?>> idConverterClass
    ) {

        if (null == object) {
            return null;
        }

        val idConverter = CLASS_ID_CONVERTER.get(idConverterClass);

        val objClass = object.getClass();
        if (CClassUtils.isJdkClass(objClass)) {
            // JDK 类（String/Integer 等）直接用对象字符串作 key
            return idConverter.apply(null, object);
        }

        // 非 JDK POJO：必须有 @CCacheId 字段，否则无法生成缓存 key
        val handle = CACHE_ID_HANDLE_CLASS_VALUE.get(objClass);
        if (null == handle) {
            throw new IllegalStateException(
                "缓存参数类型 " + objClass.getName()
                    + " 无 @CCacheId 字段且未配置 key()，无法生成缓存 key。请在方法上配置"
                    + " key=\"参数名.属性…\"（如 @CCacheable/@CCacheRemove/@CCacheUpdate），或在参数类型"
                    + " 的业务 id 字段上加 @CCacheId。namespace: "
                    + namespace.getName());
        }
        val cacheId = handle.invoke(object);
        return idConverter.apply(cacheId, object);
    }

    /**
     * 获取或创建 namespace 下指定过期时间的 Caffeine Cache
     *
     * <p><b>详细步骤</b>：取（或创建）namespace 对应的 expire→Cache 分组缓存 → 目标 expire 已存在直接返回
     * → 否则创建 Cache 并缓存：{@code expire > 0} 设 {@code expireAfterWrite(expire, SECONDS)}，
     * {@code expire = 0}（未配置）不设过期时间。</p>
     *
     * <p><b>兜底设计</b>：每个 namespace 最多创建 {@value #MAX_EXPIRE_CACHES_PER_NAMESPACE} 个不同 expire 的
     * Cache 实例，超过阈值时复用已有的最长过期时间 Cache，防止无界增长；此时实际过期时间可能与预期不一致
     * （属防御性兜底，为已知取舍）。</p>
     *
     * @param namespace 缓存命名空间类
     * @param expire    过期时间（秒），0 表示不设过期
     * @return 该 namespace 下对应过期时间的缓存实例
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
     * 获取本地缓存：未命中时执行原方法并写缓存。
     *
     * <p><b>详细步骤</b>：</p>
     * <ol>
     *   <li>取 {@code namespace()} 与 {@code expire()}，取（或创建）对应本地 Cache（见 {@link #getCache}）；</li>
     *   <li>解析缓存 key；key 为 null（无参/参数为 null/表达式某级为 null）→ 跳过缓存直接执行原方法；</li>
     *   <li>{@code getIfPresent} 命中 → 直接返回缓存值；</li>
     *   <li>未命中 → 执行原方法；结果非 null 则 {@code put} 写缓存并返回，
     *   结果 null 则记 debug 日志、不写缓存直接返回 null。</li>
     * </ol>
     *
     * <p><b>设计取舍</b>：Caffeine 的 {@code cache.get(key, mapping)} 要求 mapping 函数禁止返回 null
     * （返回 null 会抛 NPE），故改用 {@code getIfPresent + 手动 put}：方法返回 null 时不写缓存直接返回，
     * 与 Redis 路径 null 语义对齐。原子加载（单 key 并发只执行一次）仅对非空值生效，null 值不缓存、下次重新计算。</p>
     *
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

        // Caffeine cache.get 的 mapping 函数禁止返回 null（返回 null 会抛 NPE），
        // 故用 getIfPresent + 手动 put：方法返回 null 时不写缓存，与 Redis 路径 null 语义对齐
        val cached = cache.getIfPresent(cacheKey);
        if (null != cached) {
            if (log.isDebugEnabled()) {
                log.debug("命中本地缓存 cacheKey: {}", cacheKey);
            }
            return cached;
        }

        val valueNew = CAspectUtils.process(joinPoint);
        if (null != valueNew) {
            log.info("新值 cacheKey: {}, cacheValue: {}", cacheKey, valueNew);
            cache.put(cacheKey, valueNew);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("方法返回 null，不写本地缓存 cacheKey: {}", cacheKey);
            }
        }
        return valueNew;
    }

    /**
     * 获取 Redis 缓存：未命中时执行原方法并写缓存（cacheService.getCache 读-算-写一体）
     *
     * <p><b>详细步骤</b>：取 {@code namespace()} 与 {@code expire()} → 解析缓存 key
     * （null 则跳过缓存直接执行原方法）→ 拼 Redis key {@code namespace.getSimpleName() + ":" + cacheKey}
     * → 取方法返回类型（用于反序列化）→ 调 {@code cacheService.getCache} 读-算-写一并返回。</p>
     *
     * <p><b>已知限制</b>：Redis key 前缀取 {@code namespace.getSimpleName()}，
     * 不同包下的同名类可能冲突；本地缓存与 Redis 缓存的 key 语义不同（Redis 带 namespace 前缀），
     * 两种模式不可混用同一 key。</p>
     *
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

    /**
     * 删除缓存（统一入口，按 local 分流到本地 / Redis）。
     *
     * <p><b>详细步骤</b>：解析缓存 key（与 {@code @CCacheable} 同一套规则）→ key 为 null 时记 debug 日志并跳过
     * → {@code local()} 为 true 走 {@link #removeLocalCache}，否则走 {@link #removeRedisCache}。</p>
     *
     * <p><b>兜底</b>：无缓存 key（null）时不删除、不抛错。</p>
     *
     * @param joinPoint 切入点
     * @param method    目标方法
     * @param remove     缓存删除注解
     */
    public void removeCache(
        ProceedingJoinPoint joinPoint,
        Method method,
        CCacheRemove remove
    ) {

        val namespace = remove.namespace();
        val args = joinPoint.getArgs();
        val cacheKey = resolveCacheKey(args, method, namespace, remove.key(), remove.idConverter());

        if (null == cacheKey) {
            if (log.isDebugEnabled()) {
                log.debug("无缓存 key，跳过缓存删除");
            }
            return;
        }

        if (remove.local()) {
            removeLocalCache(namespace, cacheKey);
        } else {
            removeRedisCache(namespace, cacheKey);
        }
    }

    /**
     * 删除本地缓存：遍历 namespace 下所有 expire 分组的 Cache，删除对应 key。
     *
     * <p><b>详细设计</b>：本地缓存按 (namespace, expire) 分组，删除时不确定 key 落在哪个 expire 分组，
     * 故遍历该 namespace 下所有已有的 Cache 实例逐个 {@code invalidate}，保证彻底释放。</p>
     *
     * <p><b>兜底</b>：该 namespace 尚无任何 Cache 实例（{@code getIfPresent} 为 null）时直接返回。</p>
     *
     * @param namespace 缓存命名空间类
     * @param cacheKey  缓存 key
     */
    private void removeLocalCache(Class<?> namespace, String cacheKey) {

        val expireCaches = NAMESPACE_CACHES.getIfPresent(namespace);
        if (null == expireCaches) {
            return;
        }

        expireCaches.asMap().values().forEach(cache -> cache.invalidate(cacheKey));

        if (log.isDebugEnabled()) {
            log.debug("删除本地缓存，namespace: {}, cacheKey: {}", namespace.getSimpleName(), cacheKey);
        }
    }

    /**
     * 删除 Redis 缓存
     *
     * <p><b>详细设计</b>：拼 key {@code namespace.getSimpleName() + ":" + cacheKey}，
     * 交 {@code CCacheService.deleteValue} 删除（与读路径 key 格式一致，确保删得掉）。</p>
     *
     * @param namespace 缓存命名空间类
     * @param cacheKey  缓存 key
     */
    private void removeRedisCache(Class<?> namespace, String cacheKey) {

        val redisKey = namespace.getSimpleName() + ":" + cacheKey;
        cacheService.deleteValue(redisKey);

        if (log.isDebugEnabled()) {
            log.debug("删除 Redis 缓存，redisKey: {}", redisKey);
        }
    }

    /**
     * 更新缓存（统一入口，按 local 分流到本地 / Redis）。
     *
     * <p><b>详细步骤</b>：{@code result} 为 null → 记 debug 日志并跳过（避免写入空值）
     * → 解析缓存 key → key 为 null 记 debug 日志并跳过 → {@code local()} 为 true 走
     * {@link #updateLocalCache}，否则走 {@link #updateRedisCache}。</p>
     *
     * <p><b>兜底</b>：返回值 null 或无缓存 key 时跳过，不写、不抛错。</p>
     *
     * @param joinPoint 切入点
     * @param method    目标方法
     * @param update    缓存更新注解
     * @param result    方法执行结果（写入缓存的新值）
     */
    public void updateCache(
        ProceedingJoinPoint joinPoint,
        Method method,
        CCacheUpdate update,
        Object result
    ) {

        if (null == result) {
            if (log.isDebugEnabled()) {
                log.debug("方法返回 null，跳过缓存更新");
            }
            return;
        }

        val namespace = update.namespace();
        val args = joinPoint.getArgs();
        val cacheKey = resolveCacheKey(args, method, namespace, update.key(), update.idConverter());

        if (null == cacheKey) {
            if (log.isDebugEnabled()) {
                log.debug("无缓存 key，跳过缓存更新");
            }
            return;
        }

        if (update.local()) {
            updateLocalCache(namespace, update.expire(), cacheKey, result);
        } else {
            updateRedisCache(namespace, update.expire(), cacheKey, result);
        }
    }

    /**
     * 更新本地缓存：写入 namespace 下指定 expire 分组的 Cache。
     *
     * <p><b>详细设计</b>：取（或创建）namespace 下 {@code expire} 分组的 Cache 后直接 {@code put}；
     * 与删除一样按 (namespace, expire) 精确定位分组，不需要遍历。</p>
     *
     * @param namespace 缓存命名空间类
     * @param expire    过期时间（秒）
     * @param cacheKey  缓存 key
     * @param value     新值
     */
    private void updateLocalCache(Class<?> namespace, int expire, String cacheKey, Object value) {

        val cache = getCache(namespace, expire);
        cache.put(cacheKey, value);

        if (log.isDebugEnabled()) {
            log.debug("更新本地缓存，namespace: {}, cacheKey: {}, expire: {}",
                namespace.getSimpleName(), cacheKey, expire);
        }
    }

    /**
     * 更新 Redis 缓存
     *
     * <p><b>详细设计</b>：拼 key {@code namespace.getSimpleName() + ":" + cacheKey}，
     * 交 {@code CCacheService.setValue} 带过期时间写入（{@code expire} 非正数则永久）。</p>
     *
     * @param namespace 缓存命名空间类
     * @param expire    过期时间（秒）
     * @param cacheKey  缓存 key
     * @param value     新值
     */
    private void updateRedisCache(Class<?> namespace, int expire, String cacheKey, Object value) {

        val redisKey = namespace.getSimpleName() + ":" + cacheKey;
        cacheService.setValue(redisKey, value, expire);

        if (log.isDebugEnabled()) {
            log.debug("更新 Redis 缓存，redisKey: {}, expire: {}", redisKey, expire);
        }
    }

}
