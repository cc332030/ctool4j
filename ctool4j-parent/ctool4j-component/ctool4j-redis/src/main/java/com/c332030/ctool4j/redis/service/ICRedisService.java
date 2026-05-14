package com.c332030.ctool4j.redis.service;

import cn.hutool.core.util.ArrayUtil;
import com.c332030.ctool4j.core.util.CBoolUtils;
import com.c332030.ctool4j.core.validation.CAssert;
import com.c332030.ctool4j.definition.function.CFunction;
import com.c332030.ctool4j.redis.model.CValueWithTtl;
import lombok.val;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.RedisSerializer;

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

    default boolean hasKey(K key) {

        val result = getRedisTemplate().hasKey(key);
        return CBoolUtils.isTrue(result);
    }

    default boolean setIfAbsent(K key, V value) {

        val result = opsForValue()
            .setIfAbsent(key, value);
        return CBoolUtils.isTrue(result);
    }

    default boolean setIfAbsent(K key, V value, Duration timeout) {

        val result = opsForValue()
            .setIfAbsent(key, value, timeout);
        return CBoolUtils.isTrue(result);
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

    default CValueWithTtl<V> getValueWithTtl(K key) {
        return getValueWithTtl(key, CFunction.self());
    }

    default <T> CValueWithTtl<T> getValueWithTtl(K key, CFunction<V, T> convert) {

        val redisTemplate = getRedisTemplate();
        return redisTemplate.execute((RedisCallback<CValueWithTtl<T>>) connection -> {

            CAssert.notNull(key, "key is null");

            // 序列化 key
            @SuppressWarnings("unchecked")
            val keySerializer = (RedisSerializer<K>)redisTemplate.getKeySerializer();
            val keyBytes = keySerializer.serialize(key);
            CAssert.notEmpty(keyBytes, "keyBytes is null");

            // 开启管道
            connection.openPipeline();

            // 发送 GET 命令
            connection.get(keyBytes);
            // 发送 TTL 命令（秒）
            connection.ttl(keyBytes);

            // 关闭管道，获取所有结果（顺序与命令发送顺序一致）
            val results = connection.closePipeline();

            // 解析结果
            val valueBytes = (byte[]) results.get(0);
            val ttl = (Long) results.get(1);

            T value = null;
            if (ArrayUtil.isNotEmpty(valueBytes)) {

                @SuppressWarnings("unchecked")
                val valueSerializer = (RedisSerializer<V>)redisTemplate.getValueSerializer();
                val valueInRedis = valueSerializer.deserialize(valueBytes);
                value = convert.apply(valueInRedis);
            }

            return new CValueWithTtl<>(value, ttl);
        });

    }

    default Long increment(K key) {
        return opsForValue()
            .increment(key);
    }

    default Long increment(K key, long delta) {
        return opsForValue()
            .increment(key, delta);
    }

}
