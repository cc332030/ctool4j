package com.c332030.ctool4j.cache.service;

import com.c332030.ctool4j.redis.service.impl.CObjectValueRedisService;
import com.c332030.ctool4j.redis.service.impl.CLockService;
import com.c332030.ctool4j.redis.util.CLockUtils;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
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

    CObjectValueRedisService redisService;

    /**
     * 分布式同步获取值，如果不存在则计算
     * @param key 分布式锁
     * @param waitDuration 获取锁等待时长
     * @param expireDurationFunction 缓存过期时长
     * @param valueSupplier 值提供者
     * @return 值
     * @param <T> 值泛型
     */
    public <T> T computeIfAbsent(String key, Duration waitDuration,
                                 Function<T, Duration> expireDurationFunction, Supplier<T> valueSupplier) {

        T t = redisService.getValueForGenericType(key);
        if(null != t) {
            return t;
        }

        return lockService.tryLockThenRun(CLockUtils.getLockKey(key), waitDuration, () -> {

            T tNew = redisService.getValueForGenericType(key);
            if(null != tNew) {
                log.info("computeIfAbsent skip because exists value of key: {}", key);
                return tNew;
            }

            tNew = valueSupplier.get();
            Assert.notNull(tNew, "valueSupplier got null");

            Duration expireDuration = expireDurationFunction.apply(tNew);
            redisService.setValue(key, tNew, expireDuration);
            log.info("computeIfAbsent setValue successfully, key: {}, value: {}, expireDuration: {}",
                    key, tNew, expireDuration);

            return tNew;
        });
    }

}
