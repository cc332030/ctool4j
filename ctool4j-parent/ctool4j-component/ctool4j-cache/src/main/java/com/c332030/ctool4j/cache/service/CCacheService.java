package com.c332030.ctool4j.cache.service;

import com.c332030.ctool4j.redis.service.impl.CLockService;
import com.c332030.ctool4j.redis.service.impl.CStringStringRedisService;
import com.c332030.ctool4j.redis.util.CLockUtils;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.val;
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

}
