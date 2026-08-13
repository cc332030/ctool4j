package com.c332030.ctool4j.cache.service;

import com.c332030.ctool4j.core.util.CCompletableFuture;
import com.c332030.ctool4j.definition.function.CConsumer;
import com.c332030.ctool4j.redis.service.impl.CLockService;
import com.c332030.ctool4j.redis.service.impl.CStringStringRedisService;
import com.c332030.ctool4j.redis.util.CLockUtils;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.val;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>
 * Description: CCacheService
 * </p>
 *
 * @since 2025/9/26
 */
@CustomLog
@Service
@AllArgsConstructor
public class CCacheService {

    CLockService lockService;

    CStringStringRedisService redisService;

    /**
     * 分布式同步获取值，如果不存在则计算
     * @param key 分布式锁
     * @param tClass 返回值类型
     * @param waitDuration 获取锁等待时长
     * @param expireDurationFunction 缓存过期时长
     * @param valueSupplier 值提供者
     * @return 值
     * @param <T> 值泛型
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

        return lockService.tryLockThenRun(CLockUtils.getLockKey(key), waitDuration, () -> {

            T tNew = redisService.getValue(key, tClass);
            if (null != tNew) {
                log.info("computeIfAbsent skip because exists value of key: {}", key);
                return tNew;
            }

            tNew = valueSupplier.get();
            Assert.notNull(tNew, "valueSupplier got null");

            val expireDuration = expireDurationFunction.apply(tNew);
            redisService.setValue(key, tNew, expireDuration);
            log.info("computeIfAbsent setValue successfully, key: {}, expireDuration: {}",
                key, expireDuration);

            return tNew;
        });
    }

    /**
     * 设置 Redis 缓存值
     * @param key 缓存 key
     * @param value 值（将被序列化为 JSON String 存储）
     * @param expireSeconds 过期时间（秒），<=0 则永不过期
     */
    public void setValue(String key, Object value, int expireSeconds) {
        if (expireSeconds > 0) {
            redisService.setValue(key, value, Duration.ofSeconds(expireSeconds));
        } else {
            redisService.setValue(key, value);
        }
    }

    /**
     * 获取 Redis 缓存值，带过期时间
     * @param key 缓存 key
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

        val valueNew = valueSupplier.get();
        if (null != valueNew) {
            setValue(key, valueNew, expireSeconds);
        }
        return valueNew;
    }

    /**
     * 创建缓存构建器，链式配置后调用 computeIfAbsent 获取缓存
     * @param key 缓存 key
     * @param tClass 返回值类型
     * @return 缓存构建器
     * @param <T> 值泛型
     */
    public <T> CCacheBuilder<T> cacheBuilder(String key, Class<T> tClass) {
        return new CCacheBuilder<>(key, tClass);
    }

    /**
     * 缓存构建器
     * <p>缓存读取策略（按剩余存活时间 TTL 判断）：</p>
     * <ul>
     *     <li>TTL 大于刷新窗口：缓存未到期，直接返回缓存值</li>
     *     <li>TTL 大于 0 且小于等于刷新窗口（快到期）：加锁快速失败异步刷新，主线程不阻塞直接返回原值</li>
     *     <li>TTL 小于等于 0 或缓存不存在（已过期）：阻塞加锁，锁内双重检查后计算并写缓存，写后立即释放锁</li>
     * </ul>
     */
    public class CCacheBuilder<T> {

        private final String key;
        private final Class<T> tClass;

        /**
         * 等待获取锁的超时时间，默认 0 不等待
         */
        Duration waitTime = Duration.ZERO;

        /**
         * 快到期刷新窗口，默认 5 分钟：剩余存活时间小于等于该窗口时触发异步刷新
         */
        Duration refreshWindow = Duration.ofMinutes(5);

        /**
         * 异步刷新写缓存后延迟释放锁时长，默认 3 秒；正常加锁写值不延迟，直接释放
         */
        Duration unlockDelay = Duration.ofSeconds(3);

        CConsumer<RLock> onLockFail = lock -> {};

        private Function<T, Duration> expireDurationFunction;

        private CCacheBuilder(String key, Class<T> tClass) {
            this.key = key;
            this.tClass = tClass;
        }

        /**
         * 等待获取锁的超时时间（秒），默认 0 不等待
         */
        public CCacheBuilder<T> waitTime(long waitTime) {
            return waitTime(Duration.ofSeconds(waitTime));
        }

        /**
         * 等待获取锁的超时时间
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
         * 缓存过期时长
         * @param expireDurationFunction 根据值计算过期时长
         * @return this
         */
        public CCacheBuilder<T> expireDuration(Function<T, Duration> expireDurationFunction) {
            this.expireDurationFunction = expireDurationFunction;
            return this;
        }

        /**
         * 快到期刷新窗口，剩余存活时间小于等于该窗口时，加锁快速失败并异步刷新
         * @param refreshWindow 刷新窗口，默认 5 分钟
         * @return this
         */
        public CCacheBuilder<T> refreshWindow(Duration refreshWindow) {
            this.refreshWindow = refreshWindow;
            return this;
        }

        /**
         * 异步刷新写缓存后延迟释放锁，避免其他线程在刷新生效的瞬间窗口内读到旧数据
         * <p>仅异步刷新路径生效，正常加锁写值直接释放锁</p>
         * @param unlockDelay 释放锁前延迟时间，默认 3 秒
         * @return this
         */
        public CCacheBuilder<T> unlockDelay(Duration unlockDelay) {
            this.unlockDelay = unlockDelay;
            return this;
        }

        /**
         * 获取缓存，按剩余存活时间分流：
         * <ul>
         *     <li>未到期且剩余存活时间大于刷新窗口：直接返回缓存值</li>
         *     <li>快到期（剩余存活时间小于等于刷新窗口）：异步刷新并立即返回原值，主线程不阻塞</li>
         *     <li>已到期或缓存不存在：阻塞加锁，锁内双重检查后计算写缓存，写后立即释放锁</li>
         * </ul>
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

            return lockBuilder.execute(() -> {

                T tNew = redisService.getValue(key, tClass);
                if (null != tNew) {
                    log.debug("cacheBuilder computeIfAbsent skip because exists value of key: {}", key);
                    return tNew;
                }

                tNew = valueSupplier.get();
                Assert.notNull(tNew, "valueSupplier got null");

                if (null == expireDurationFunction) {
                    redisService.setValue(key, tNew);
                    log.debug("cacheBuilder computeIfAbsent setValue successfully, key: {}", key);
                } else {
                    val expireDuration = expireDurationFunction.apply(tNew);
                    redisService.setValue(key, tNew, expireDuration);
                    log.debug("cacheBuilder computeIfAbsent setValue successfully, key: {}, expireDuration: {}",
                        key, expireDuration);
                }

                return tNew;
            });
        }

        /**
         * 快到期异步刷新：加锁快速失败（不等待锁），锁内双重检查后取数写缓存
         * <p>刷新写缓存成功后延迟 unlockDelay（默认 3 秒）再释放锁，避免其他线程在刷新生效的瞬间窗口内读到旧数据</p>
         */
        private void refreshAsync(Supplier<T> valueSupplier) {

            CCompletableFuture.runAsync(() ->
                lockService.lock(CLockUtils.getLockKey(key))
                    .waitTime(Duration.ZERO)
                    .unlockDelay(unlockDelay)
                    .execute(() -> {

                        // 双重检查：锁内可能已被其他线程刷新
                        val currentWithTtl = redisService.getValueWithTtl(key, tClass);
                        if (null != currentWithTtl
                            && null != currentWithTtl.getTtl()
                            && currentWithTtl.getTtl() * 1000 > refreshWindow.toMillis()
                        ) {
                            log.debug("cacheBuilder refreshAsync skip because refreshed by other thread, key: {}", key);
                            return null;
                        }

                        T tNew = valueSupplier.get();
                        if (null == tNew) {
                            return null;
                        }

                        if (null == expireDurationFunction) {
                            redisService.setValue(key, tNew);
                        } else {
                            redisService.setValue(key, tNew, expireDurationFunction.apply(tNew));
                        }
                        log.debug("cacheBuilder refreshAsync refresh successfully, key: {}", key);

                        return null;
                    })
            );
        }
    }


}
