package com.c332030.ctool4j.redis.service;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Description: CAbstractRedisService
 * </p>
 *
 * @since 2024/3/8
 */
public abstract class CAbstractRedisService<K, V> {

    @Setter(onMethod = @__({@Autowired}))
    RedisTemplate<K, V> redisTemplate;

    public ValueOperations<K, V> opsForValue() {
        return redisTemplate.opsForValue();
    }

    public void setValue(K key, V value) {
        opsForValue().set(key, value);
    }
    public void setValue(K key, V value, long timeout, TimeUnit unit) {
        opsForValue().set(key, value, timeout, unit);
    }
    public void setValue(K key, V value, Duration timeout) {
        opsForValue().set(key, value, timeout);
    }

    public V getValue(K key) {
        return opsForValue().get(key);
    }

    public void delete(K key) {
        redisTemplate.delete(key);
    }

}
