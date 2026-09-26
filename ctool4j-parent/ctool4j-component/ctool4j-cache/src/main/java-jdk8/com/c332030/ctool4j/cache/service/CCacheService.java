package com.c332030.ctool4j.cache.service;

import com.c332030.ctool4j.core.util.CThreadUtils;
import com.c332030.ctool4j.definition.function.CConsumer;
import com.c332030.ctool4j.redis.service.impl.CLockService;
import com.c332030.ctool4j.redis.service.impl.CStringStringRedisService;
import com.c332030.ctool4j.redis.util.CLockUtils;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.val;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>
 * Description: CCacheService
 * </p>
 *
 * <p>{@code @Service} 的 Redis 缓存服务：提供缓存读写、加锁双重检查的读-算-写（防缓存击穿），
 * 以及按剩余 TTL 分流、快到期异步刷新的缓存构建器 {@code CCacheBuilder}。</p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@link #setValue(String, Object, int)} / {@link #deleteValue(String)}：写、删缓存。</li>
 *   <li>{@link #cacheBuilder(String, Class)}：返回 {@code CCacheBuilder<T>}（waitTime / onLockFail /
 *   expireDuration / refreshWindow 链式配置，按剩余 TTL 分流：未到期直接返回、快到期异步刷新、已到期阻塞加锁）。</li>
 * </ul>
 *
 * <h2>设计思路总述</h2>
 * <ul>
 *   <li><b>防击穿</b>：未命中时加分布式锁，锁内双重检查后计算并写缓存；获取锁失败降级为无锁直接计算
 *   （避免误判缓存值不存在）。</li>
 *   <li><b>防雪崩/平滑续期</b>：{@code CCacheBuilder} 按剩余 TTL 分流——TTL 大于刷新窗口直接返回、
 *   TTL 在 (0, 刷新窗口] 加锁快速失败异步刷新（主线程不阻塞）、TTL ≤ 0 或不存在则阻塞加锁读-算-写。</li>
 *   <li><b>异步刷新抑制</b>：{@code refreshing} 标记防并发 + {@code lastRefreshMillis} 节流（10s），
 *   状态按 key 存于静态 Caffeine LoadingCache（空闲 5 分钟淘汰），跨 builder 实例持久生效；
 *   刷新走独立 daemon 线程池，不占 commonPool。</li>
 *   <li><b>null 语义统一</b>：计算值为 null 一律不写缓存、直接返回（避免缓存空值）。</li>
 * </ul>
 * <p>详细设计、详细步骤、兜底与已知限制见各方法与内部类 javadoc。</p>
 *
 * @since 2025/9/26
 * @version 1.0
 */
@CustomLog
@Service
@AllArgsConstructor
public class CCacheService {

    /**
     * 异步刷新专用线程池：daemon 线程不阻塞 JVM 退出；独立于 commonPool，避免与业务并行流抢占。
     * <p>实例字段而非 static：由本实例的 {@link #destroy()} 随容器销毁关闭，
     * 避免多 Spring 上下文（含单元测试多上下文）互相影响</p>
     */
    final ExecutorService REFRESH_EXECUTOR = Executors.newFixedThreadPool(
        Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
        r -> CThreadUtils.newDaemonThread(r, "cache-refresh")
    );

    /**
     * 异步刷新最小间隔（毫秒）：间隔内最多发起一次刷新，失败也算
     */
    private static final long REFRESH_INTERVAL_MILLIS = 10_000L;

    /**
     * 已过期路径默认等锁超时：缓存过期并发时等待持锁线程写完缓存后读新值，
     * 避免默认不等待直接抢锁失败抛异常导致业务失败
     */
    private static final Duration DEFAULT_WAIT_TIME = Duration.ofSeconds(1);

    /**
     * 刷新状态空闲淘汰时长：key 停止被访问后自动回收，避免动态 key 场景内存泄露
     */
    private static final Duration REFRESH_STATE_IDLE_DURATION = Duration.ofMinutes(5);

    /**
     * 按 key 的异步刷新状态：独立于 builder 实例（builder 每次 new，实例字段无法跨调用持久），
     * 保证节流与防并发跨调用生效；Caffeine 空闲淘汰，key 停用后自动释放，避免内存泄露。
     * <p>使用 LoadingCache 而非 Cache.get(key, function)：后者并发探测时 mapping 函数可能被多次执行、
     * 各线程拿到各自实例，导致 refreshing/lastRefreshMillis 状态分叉（防并发与节流失效）；
     * LoadingCache.get 保证每 key 单实例</p>
     * <p>已知取舍（KNOWN）：刷新任务运行超过空闲淘汰时长（5 分钟）而被淘汰时，refreshing/lastRefreshMillis
     * 状态丢失，节流与防并发标记失效，极端情况下可能并发重复刷新。属可接受的边界取舍，由使用者自行处理</p>
     */
    private static final LoadingCache<String, RefreshState> REFRESH_STATES = Caffeine.newBuilder()
        .expireAfterAccess(REFRESH_STATE_IDLE_DURATION)
        .build(key -> new RefreshState());

    /**
     * 单个 key 的异步刷新状态
     */
    private static final class RefreshState {

        /**
         * 刷新进行中标记：同一 key 同时只允许一个异步刷新任务，避免并发重复计算
         */
        final AtomicBoolean refreshing = new AtomicBoolean();

        /**
         * 最近一次发起刷新的时间戳（毫秒）：刷新间隔节流，无论成功失败，间隔内不再发起
         */
        final AtomicLong lastRefreshMillis = new AtomicLong();
    }

    CLockService lockService;

    CStringStringRedisService redisService;

    /**
     * 容器销毁时关闭异步刷新线程池：允许已提交任务执行完（shutdown 不中断），
     * daemon 线程本就随 JVM 退出，此处保证资源有序释放
     *
     * <ul>
     *   <li>{@link #destroy()}：容器销毁时关闭异步刷新线程池。</li>
     * </ul>
     */
    @PreDestroy
    public void destroy() {
        REFRESH_EXECUTOR.shutdown();
    }

    /**
     * 分布式同步获取值，如果不存在则计算
     *
     * <p><b>详细步骤</b>：先直接读缓存，命中即返回 → 未命中调用
     * {@code lockService.tryLockThenRun} 在锁内执行 {@link #computeAndWrite}（双重检查 + 非空强校验 + 写缓存）。</p>
     *
     * <p><b>已废弃</b>：旧接口，缺少按 TTL 分流与异步刷新能力，建议改用 {@link #cacheBuilder(String, Class)}。
     * 保留期间行为不变，调用处不迁移（内部废弃 API 调用不修复）。</p>
     * <ul>
     *   <li>{@link #computeIfAbsent(String, Class, Duration, Function, Supplier)}（已废弃）：分布式同步读-算-写。</li>
     * </ul>
     *
     * @param key 缓存 key
     * @param tClass 返回值类型
     * @param waitDuration 获取锁等待时长
     * @param expireDurationFunction 根据值计算缓存过期时长
     * @param valueSupplier 值提供者
     * @return 值
     * @param <T> 值泛型
     * @deprecated 改用 {@link #cacheBuilder(String, Class)}
     */
    @Deprecated
    public <T> T computeIfAbsent(
        String key, Class<T> tClass,
        Duration waitDuration,
        Function<T, Duration> expireDurationFunction,
        Supplier<T> valueSupplier
    ) {

        T t = redisService.getValue(key, tClass);
        if (null != t) {
            return t;
        }

        return lockService.tryLockThenRun(CLockUtils.getLockKey(key), waitDuration,
            () -> computeAndWrite(key, tClass, valueSupplier, expireDurationFunction));
    }

    /**
     * 双重检查后计算并写缓存（"读-算-写"公共逻辑）
     *
     * <p><b>详细步骤</b>：锁内读缓存 → 命中记 debug 日志并直接返回 → 取新值
     * → {@code Assert.notNull} 强校验（null 视为计算失败，抛异常）→ 按值计算过期时长
     * → 写缓存并记 debug 日志 → 返回新值。</p>
     *
     * <p><b>复用范围</b>：供锁内场景复用（已废弃的
     * {@link #computeIfAbsent(String, Class, Duration, Function, Supplier)}、
     * {@link CCacheBuilder#computeIfAbsent(Supplier)}）；
     * {@link #getCache(String, Class, int, Supplier)} 无锁且允许 null 值，语义不同，独立实现不复用。</p>
     *
     * @param key                    缓存 key
     * @param tClass                 返回值类型
     * @param valueSupplier          值提供者（结果非空校验，null 视为计算失败）
     * @param expireDurationFunction 根据值计算缓存过期时长
     * @param <T>                    值泛型
     * @return 缓存值
     */
    private <T> T computeAndWrite(String key, Class<T> tClass, Supplier<T> valueSupplier, Function<T, Duration> expireDurationFunction) {
        T tNew = redisService.getValue(key, tClass);
        if (null != tNew) {
            log.debug("computeAndWrite skip because exists value of key: {}", key);
            return tNew;
        }
        tNew = valueSupplier.get();
        Assert.notNull(tNew, "valueSupplier got null");
        val cacheDuration = expireDurationFunction.apply(tNew);
        redisService.setValue(key, tNew, cacheDuration);
        log.debug("computeAndWrite setValue successfully, key: {}, cacheDuration: {}", key, cacheDuration);
        return tNew;
    }

    /**
     * 设置 Redis 缓存值
     *
     * <p><b>详细设计</b>：{@code expireSeconds > 0} 时带过期时长写入；非正数（含 0）时按永久写入，
     * 供 {@code @CCacheable(expire = 0)} / {@code @CCacheUpdate(expire = 0)} 的"永久缓存"语义落地。</p>
     *
     * @param key 缓存 key
     * @param value 值（将被序列化为 JSON String 存储）
     * @param expireSeconds 过期时间（秒），非正数则永不过期
     */
    public void setValue(String key, Object value, int expireSeconds) {
        if (expireSeconds > 0) {
            redisService.setValue(key, value, Duration.ofSeconds(expireSeconds));
        } else {
            redisService.setValue(key, value);
        }
    }

    /**
     * 删除 Redis 缓存值
     *
     * <p><b>详细设计</b>：直接委托 {@code redisService.delete}，供 {@code @CCacheRemove} 切面调用；
     * key 不存在时删除无副作用。</p>
     *
     * @param key 缓存 key
     */
    public void deleteValue(String key) {
        redisService.delete(key);
    }

    /**
     * 获取 Redis 缓存值（未命中时加锁双重检查计算并写缓存）
     *
     * <p><b>详细步骤</b>：</p>
     * <ol>
     *   <li>直接读缓存：命中记 debug 日志并返回；</li>
     *   <li>未命中记 debug 日志，用 {@code AtomicBoolean} 记录加锁是否失败，调
     *   {@code lockService.lock(...).waitTime(默认 1s).onLockFail(标记失败)} 加锁执行：
     *   锁内重温缓存（命中直接返回）→ 取新值 → 非 null 才写缓存 → 返回新值；</li>
     *   <li>未发生加锁失败 → 返回锁内结果；</li>
     *   <li>加锁失败 → 降级无锁直接计算：取新值、非 null 写缓存、返回（避免误判缓存值不存在，
     *   缓存击穿退化为旧的无锁读-算-写）。</li>
     * </ol>
     *
     * <p><b>设计取舍</b>：与 {@link #computeAndWrite(String, Class, Supplier, Function)} 语义不同
     * （后者锁内强校验非空），故独立实现不复用；值提供者允许返回 null，null 时不写缓存直接返回，
     * 避免缓存空值。默认等锁 1s（{@code DEFAULT_WAIT_TIME}），使缓存过期时的并发访问能读到持锁线程写入的新值，
     * 避免抢锁失败抛异常。</p>
     * <ul>
     *   <li>{@link #getCache(String, Class, int, Supplier)}：加锁双重检查的读-算-写（允许 null 值）。</li>
     * </ul>
     *
     * @param key 缓存 key
     * @param tClass 返回值类型
     * @param expireSeconds 过期时间（秒）
     * @param valueSupplier 值提供者
     * @return 值
     * @param <T> 值泛型
     */
    public <T> T getCache(String key, Class<T> tClass, int expireSeconds, Supplier<T> valueSupplier) {

        val value = redisService.getValue(key, tClass);
        if (null != value) {
            if (log.isDebugEnabled()) {
                log.debug("命中 Redis 缓存，key: {}", key);
            }
            return value;
        }

        if (log.isDebugEnabled()) {
            log.debug("Redis 缓存未命中，key: {}", key);
        }

        // 加锁双重检查防缓存击穿：并发未命中时只放一个线程计算写缓存，其余等锁后读新值
        val lockFail = new AtomicBoolean(false);
        val result = lockService.lock(CLockUtils.getLockKey(key))
            .waitTime(DEFAULT_WAIT_TIME)
            .onLockFail(unused -> lockFail.set(true))
            .execute(() -> {
                // 双重检查：锁内可能已被其他线程刷新写入
                val valueInLock = redisService.getValue(key, tClass);
                if (null != valueInLock) {
                    if (log.isDebugEnabled()) {
                        log.debug("锁内命中 Redis 缓存，key: {}", key);
                    }
                    return valueInLock;
                }
                val valueNew = valueSupplier.get();
                if (null != valueNew) {
                    setValue(key, valueNew, expireSeconds);
                }
                return valueNew;
            });

        if (!lockFail.get()) {
            return result;
        }
        // 获取锁失败：降级无锁直接计算，避免误判缓存值不存在（缓存击穿退化为旧的无锁读-算-写）
        if (log.isDebugEnabled()) {
            log.debug("获取锁失败，降级无锁计算，key: {}", key);
        }
        val valueFallback = valueSupplier.get();
        if (null != valueFallback) {
            setValue(key, valueFallback, expireSeconds);
        }
        return valueFallback;
    }

    /**
     * 创建缓存构建器，链式配置后调用 computeIfAbsent 获取缓存
     *
     * <p><b>详细设计</b>：返回内部类 {@code CCacheBuilder<T>}，其默认值为等锁 1s、刷新窗口 5 分钟、
     * 默认过期 23 小时，均可经链式方法覆盖；builder 每次调用新建，而异步刷新状态按 key 存于静态
     * {@code REFRESH_STATES}，因此节流与防并发跨 builder 实例生效。</p>
     *
     * @param key 缓存 key
     * @param tClass 返回值类型
     * @return 缓存构建器
     * @param <T> 值泛型
     */
    public <T> CCacheBuilder<T> cacheBuilder(String key, Class<T> tClass) {
        return new CCacheBuilder<>(key, tClass);
    }

    /**
     * 缓存构建器：链式配置后经 {@link #computeIfAbsent(Supplier)} 读取缓存
     *
     * <p><b>详细设计</b>：读取策略按剩余存活时间（TTL）分流——</p>
     * <ul>
     *     <li>TTL 小于 0（{@code -1}，永久缓存）：直接返回缓存值，避免每次访问都走加锁计算；</li>
     *     <li>TTL 大于刷新窗口：缓存未到期，直接返回缓存值；</li>
     *     <li>TTL 大于 0 且小于等于刷新窗口（快到期）：加锁快速失败异步刷新，主线程不阻塞直接返回原值；</li>
     *     <li>TTL 小于等于 0 或缓存不存在（已过期/未写入）：阻塞加锁（默认等 1s），
     *     锁内双重检查后计算并写缓存，写后立即释放锁。</li>
     * </ul>
     *
     * <p><b>可配置项</b>：{@code waitTime}（等锁时长）、{@code onLockFail}（抢锁失败回调）、
     * {@code expireDuration}（固定或按值动态计算的过期时长，动态优先）、{@code refreshWindow}（刷新窗口）。</p>
     */
    public class CCacheBuilder<T> {

        private final String key;
        private final Class<T> tClass;

        /**
         * 等待获取锁的超时时间，默认等待（缓存已过期路径需阻塞等锁读新值，避免抢锁失败抛异常）
         */
        Duration waitTime = DEFAULT_WAIT_TIME;

        /**
         * 快到期刷新窗口：剩余存活时间小于等于该窗口时触发异步刷新
         */
        Duration refreshWindow = Duration.ofMinutes(5);

        /**
         * 获取锁失败回调，默认空操作
         */
        CConsumer<RLock> onLockFail = lock -> {};

        /**
         * 缓存默认过期时长：未配置 expireDuration(Function) 动态计算时生效
         */
        Duration expireDuration = Duration.ofHours(23);

        private Function<T, Duration> expireDurationFunction;

        private CCacheBuilder(String key, Class<T> tClass) {
            this.key = key;
            this.tClass = tClass;
        }

        /**
         * 等待获取锁的超时时间（秒），默认等待
         * @param waitTime 等待秒数
         * @return this
         */
        public CCacheBuilder<T> waitTime(long waitTime) {
            return waitTime(Duration.ofSeconds(waitTime));
        }

        /**
         * 等待获取锁的超时时间
         * @param waitDuration 等待时长
         * @return this
         */
        public CCacheBuilder<T> waitTime(Duration waitDuration) {
            this.waitTime = waitDuration;
            return this;
        }

        /**
         * 获取锁失败回调
         * @param onLockFail 获取锁失败时执行的回调，参数为锁对象
         * @return this
         */
        public CCacheBuilder<T> onLockFail(CConsumer<RLock> onLockFail) {
            this.onLockFail = onLockFail;
            return this;
        }

        /**
         * 缓存过期时长，直接传值（与 Function 二选一）
         * <p>同时配置 Function 时方法优先，此值不生效；均未配置时使用字段默认值</p>
         * @param expireDuration 过期时长
         * @return this
         */
        public CCacheBuilder<T> expireDuration(Duration expireDuration) {
            this.expireDuration = expireDuration;
            return this;
        }

        /**
         * 缓存过期时长，按值动态计算（与直接传值二选一，方法优先）
         * @param expireDurationFunction 根据值计算过期时长
         * @return this
         */
        public CCacheBuilder<T> expireDuration(Function<T, Duration> expireDurationFunction) {
            this.expireDurationFunction = expireDurationFunction;
            return this;
        }

        /**
         * 快到期刷新窗口，剩余存活时间小于等于该窗口时，加锁快速失败并异步刷新
         * @param refreshWindow 刷新窗口，未配置时使用字段默认值
         * @return this
         */
        public CCacheBuilder<T> refreshWindow(Duration refreshWindow) {
            this.refreshWindow = refreshWindow;
            return this;
        }

        /**
         * 获取缓存，按剩余存活时间分流
         *
         * <p><b>详细步骤</b>：</p>
         * <ol>
         *   <li>读缓存并取 TTL；</li>
         *   <li>TTL 为负（{@code -1} 永久缓存）→ 直接返回缓存值，避免每次访问走加锁计算；</li>
         *   <li>TTL 大于刷新窗口（未到期）→ 直接返回缓存值；</li>
         *   <li>TTL 大于 0 且小于等于刷新窗口（快到期）→ 调 {@code refreshAsync} 加锁快速失败异步刷新，
         *   主线程不阻塞、立即返回原值；</li>
         *   <li>TTL 小于等于 0 或缓存不存在 → 阻塞加锁（等 {@code waitTime}），
         *   锁内执行 {@code computeAndWrite}（双重检查 + 非空强校验 + 写缓存），写后立即释放锁。</li>
         * </ol>
         *
         * @param valueSupplier 值提供者
         * @return 值
         */
        public T computeIfAbsent(Supplier<T> valueSupplier) {

            val valueWithTtl = redisService.getValueWithTtl(key, tClass);

            if (null != valueWithTtl
                && null != valueWithTtl.getValue()
                && null != valueWithTtl.getTtl()
            ) {
                val ttlMillis = valueWithTtl.getTtl() * 1000;
                if (ttlMillis < 0) {
                    // TTL=-1 表示 key 存在且无过期时间（永久缓存）：直接返回缓存值，
                    // 避免每次访问都走加锁计算路径导致缓存形同虚设（Q6）
                    return valueWithTtl.getValue();
                }
                if (ttlMillis > refreshWindow.toMillis()) {
                    // 未到期且未进入刷新窗口，直接返回原值
                    return valueWithTtl.getValue();
                }
                if (ttlMillis > 0) {
                    // 快到期：加锁快速失败异步刷新，主线程不阻塞，返回原值
                    refreshAsync(valueSupplier);
                    return valueWithTtl.getValue();
                }
            }

            // 已到期或缓存不存在：阻塞加锁获取
            val lockBuilder = lockService.lock(CLockUtils.getLockKey(key))
                .waitTime(waitTime)
                .onLockFail(onLockFail);

            return lockBuilder.execute(() -> computeAndWrite(key, tClass, valueSupplier,
                t -> null == expireDurationFunction ? expireDuration : expireDurationFunction.apply(t)));
        }

        /**
         * 快到期异步刷新：加锁快速失败（不等待锁），锁内双重检查后取数写缓存
         *
         * <p><b>详细步骤</b>：</p>
         * <ol>
         *   <li>两层抑制（状态按 key 存于静态 {@code REFRESH_STATES}，跨 builder 实例生效）：
         *   {@code refreshing} 标记防并发（同一 key 同时只允许一个刷新任务）+
         *   {@code lastRefreshMillis} 节流（刷新最小间隔内最多发起一次，失败也算），
         *   命中任一抑制即记 debug 日志并返回；</li>
         *   <li>发起时即记录时间戳（刷新失败也算一次）；</li>
         *   <li>提交独立 daemon 线程池：加锁（不等待，抢锁失败视为正常竞争，记 debug 跳过不抛异常）
         *   → 锁内双重检查（TTL 为 {@code -1} 永久缓存或已被其他线程刷新到未到期则跳过；
         *   TTL 为 {@code -2} 即 key 不存在时继续计算）→ 取新值（null 跳过，不缓存空值）
         *   → 按值计算过期时长 → 写缓存并记 debug 日志；</li>
         *   <li>异常记 error 日志不静默丢弃，{@code finally} 复位 {@code refreshing} 标记。</li>
         * </ol>
         */
        private void refreshAsync(Supplier<T> valueSupplier) {

            val refreshState = REFRESH_STATES.get(key);

            val now = System.currentTimeMillis();
            // 间隔节流：无论上次刷新成功与否，间隔内不再发起
            if (now - refreshState.lastRefreshMillis.get() < REFRESH_INTERVAL_MILLIS) {
                log.debug("cacheBuilder refreshAsync skip because within refresh interval, key: {}", key);
                return;
            }

            if (!refreshState.refreshing.compareAndSet(false, true)) {
                log.debug("cacheBuilder refreshAsync skip because refreshing, key: {}", key);
                return;
            }
            // 发起时即记录时间：刷新失败也算一次，间隔内不再重试
            refreshState.lastRefreshMillis.set(now);

            REFRESH_EXECUTOR.execute(() -> {
                try {
                    lockService.lock(CLockUtils.getLockKey(key))
                        .waitTime(Duration.ZERO)
                        // 抢锁失败是正常竞争（其他线程/实例正在刷新），打 debug 跳过，不抛异常
                        .onLockFail(lock -> log.debug("cacheBuilder refreshAsync skip because lock fail, key: {}", key))
                        .execute(() -> {

                            // 双重检查：锁内可能已被其他线程刷新（未到期）；TTL=-1（永久缓存）也无需刷新；
                            // TTL=-2（key 不存在）不在此分支，继续计算写缓存（Q6）
                            val currentWithTtl = redisService.getValueWithTtl(key, tClass);
                            val currentTtl = null == currentWithTtl ? null : currentWithTtl.getTtl();
                            if (null != currentTtl
                                && (currentTtl == -1L || currentTtl > 0 && currentTtl * 1000 > refreshWindow.toMillis())
                            ) {
                                log.debug("cacheBuilder refreshAsync skip because refreshed by other thread, key: {}", key);
                                return null;
                            }

                            T tNew = valueSupplier.get();
                            if (null == tNew) {
                                return null;
                            }

                            val cacheDuration = null == expireDurationFunction
                                ? expireDuration
                                : expireDurationFunction.apply(tNew);
                            redisService.setValue(key, tNew, cacheDuration);
                            log.debug("cacheBuilder refreshAsync refresh successfully, key: {}, cacheDuration: {}",
                                key, cacheDuration);

                            return null;
                        });
                } catch (Throwable e) {
                    log.error("cacheBuilder refreshAsync error, key: {}", key, e);
                } finally {
                    refreshState.refreshing.set(false);
                }
            });
        }
    }

}
