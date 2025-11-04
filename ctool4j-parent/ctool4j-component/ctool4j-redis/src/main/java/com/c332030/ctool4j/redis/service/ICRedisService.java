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

    /**
     * 获取 RedisTemplate
     * @return RedisTemplate
     */
    RedisTemplate<K, V> getRedisTemplate();

    /**
     * 判断 key 是否无效
     * @param key key
     * @return 有效性
     */
    default boolean isInvalidKey(K key) {
        return Objects.isNull(key);
    }

    /**
     * 值无效
     * @param value 值
     * @return 有效性
     */
    default boolean isInvalidValue(V value) {
        return Objects.isNull(value);
    }

    /**
     * 获取 ValueOperations
     * @return ValueOperations
     */
    default ValueOperations<K, V> opsForValue() {
        return getRedisTemplate().opsForValue();
    }

    /**
     * 设置值
     * @param key key
     * @param value 值
     */
    default void setValue(K key, V value) {

        if(isInvalidKey(key) || isInvalidValue(value)) {
            return;
        }

        opsForValue().set(key, value);
    }

    /**
     * 设置值
     * @param key key
     * @param value 值
     * @param timeout 超时时间
     * @param unit 时间单位
     */
    default void setValue(K key, V value, long timeout, TimeUnit unit) {

        if(isInvalidKey(key)
                || isInvalidValue(value)
                || timeout <= 0
        ) {
            return;
        }

        opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 设置值
     * @param key key
     * @param value 值
     * @param timeout 超时
     */
    default void setValue(K key, V value, Duration timeout) {

        if(isInvalidKey(key) || isInvalidValue(value)) {
            return;
        }

        opsForValue().set(key, value, timeout);
    }

    /**
     * 获取值
     * @param key key
     * @return 值
     */
    default V getValue(K key) {

        if(isInvalidKey(key)) {
            return null;
        }

        return opsForValue().get(key);
    }

    /**
     * 获取值
     * @param key key
     * @param convert 转换函数
     * @return 值
     * @param <T> 转换类型
     */
    @SneakyThrows
    default <T> T getValue(K key, CFunction<V, T> convert) {

        val value = getValue(key);
        if(isInvalidValue(value)) {
            return null;
        }

        return convert.apply(value);
    }

    /**
     * 删除
     * @param key key
     */
    default void delete(K key) {

        if(isInvalidKey(key)) {
            return;
        }
        getRedisTemplate().delete(key);
    }

}
