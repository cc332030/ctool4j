package com.c332030.ctool4j.redis.service;

import com.c332030.ctool4j.core.function.CFunction;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Objects;
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

    default boolean isInvalidKey(K key) {
        return Objects.isNull(key);
    }

    default boolean isInvalidValue(V value) {
        return Objects.isNull(value);
    }

    default ValueOperations<K, V> opsForValue() {
        return getRedisTemplate().opsForValue();
    }

    default void setValue(K key, V value) {

        if(isInvalidKey(key) || isInvalidValue(value)) {
            return;
        }

        opsForValue().set(key, value);
    }
    default void setValue(K key, V value, long timeout, TimeUnit unit) {

        if(isInvalidKey(key)
                || isInvalidValue(value)
                || timeout <= 0
        ) {
            return;
        }

        opsForValue().set(key, value, timeout, unit);
    }
    default void setValue(K key, V value, Duration timeout) {

        if(isInvalidKey(key) || isInvalidValue(value)) {
            return;
        }

        opsForValue().set(key, value, timeout);
    }

    default V getValue(K key) {

        if(isInvalidKey(key)) {
            return null;
        }

        return opsForValue().get(key);
    }

    @SneakyThrows
    default <T> T getValue(K key, CFunction<V, T> function) {

        val value = getValue(key);
        if(isInvalidValue(value)) {
            return null;
        }

        return function.apply(value);
    }

    default void delete(K key) {

        if(isInvalidKey(key)) {
            return;
        }
        getRedisTemplate().delete(key);
    }

}
