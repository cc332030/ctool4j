package com.c332030.ctool4j.redis.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Description: ICRedisService
 * </p>
 *
 * @since 2025/11/4
 */
public interface ICRedisService<K, V> {

    RedisTemplate<K, V> getRedisTemplate();

    default ValueOperations<K, V> opsForValue() {
        return getRedisTemplate().opsForValue();
    }

    default void setValue(K key, V value) {
        opsForValue().set(key, value);
    }
    default void setValue(K key, V value, long timeout, TimeUnit unit) {
        opsForValue().set(key, value, timeout, unit);
    }
    default void setValue(K key, V value, Duration timeout) {
        opsForValue().set(key, value, timeout);
    }

    default V getValue(K key) {
        return opsForValue().get(key);
    }

    default void delete(K key) {
        getRedisTemplate().delete(key);
    }

}
