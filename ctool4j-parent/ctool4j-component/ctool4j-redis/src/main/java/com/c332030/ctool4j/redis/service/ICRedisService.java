package com.c332030.ctool4j.redis.service;

import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
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
 * <h2>能力目录</h2>
 * <p>{@code ICRedisService&lt;K, V&gt;} 为 Redis 键值操作的统一抽象接口，基于 Spring Data Redis {@code RedisTemplate}， 提供大量默认方法：增删改查、判断 key 存在、setIfAbsent、值 + 剩余 TTL 读取、自增等。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>key/value 无效</td>
 *     <td>setValue 静默返回，getValue 返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>timeout&lt;=0</td>
 *     <td>setValue(带超时) 不写入</td>
 *   </tr>
 *   <tr>
 *     <td>值无效 + convert</td>
 *     <td>getValue(key, convert, default) 返回 default</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>各种 Redis 键值读写统一封装，作为 {@code CAbstractRedisService} 及具体 Service 的基础。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不涉及 hash/list/set 等复杂结构；仅字符串值操作。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>管道解析假设 GET/TTL 返回顺序固定（值字节、TTL），依赖 Redis 版本行为一致。</li>
 *   <li>key 序列化要求（{@code getValueWithTtl}）依赖模板的 keySerializer 能 serialize 出非空字节。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义约定</b></p>
 * <ul>
 *   <li>{@code isInvalidKey}/{@code isInvalidValue} 用于判空，默认用 {@code Objects.isNull}；子类可重写（如 String 场景按空白判断）。</li>
 *   <li>{@code setValue} 系列在 key/value 无效或 timeout&lt;=0 时静默返回（不写、不抛）。</li>
 *   <li>{@code getValueWithTtl} 用 Redis 管道（pipeline）一次往返获取 GET + TTL，避免两次网络调用。</li>
 * </ul>
 * <p><b>实现方式</b></p>
 * <ul>
 *   <li>默认方法基于 {@code getRedisTemplate()} 抽象方法，子类提供模板。</li>
 *   <li>{@code getValueWithTtl} 通过 {@code RedisCallback} + {@code openPipeline} 串行执行 GET/TTL，结果按命令顺序解析。</li>
 *   <li>{@code getValue(key, convert, default)} 在值无效时返回 defaultValue。</li>
 * </ul>
 *
 * @since 2025/11/4
 * @version 1.0
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
     * 判断 key 是否存在
     * @param key key
     * @return 是否存在
     */
    default boolean hasKey(K key) {

        val result = getRedisTemplate().hasKey(key);
        return CBoolUtils.isTrue(result);
    }

    /**
     * 键不存在时设置值
     * @param key key
     * @param value 值
     * @return 是否设置成功
     */
    default boolean setIfAbsent(K key, V value) {

        val result = opsForValue()
            .setIfAbsent(key, value);
        return CBoolUtils.isTrue(result);
    }

    /**
     * 键不存在时设置值（带超时）
     * @param key key
     * @param value 值
     * @param timeout 超时
     * @return 是否设置成功
     */
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
     * @return 值 Optional
     */
    default Opt<V> getValueOpt(K key){
        return Opt.ofNullable(getValue(key));
    }

    /**
     * 获取值
     * @param key key
     * @param convert 转换函数
     * @return 值
     * @param <T> 转换类型
     */
    default <T> T getValue(K key, CFunction<V, T> convert) {
        return getValue(key, convert, null);
    }

    /**
     * 获取值
     * @param key key
     * @param convert 转换函数
     * @param defaultValue 默认值
     * @return 值
     * @param <T> 转换类型
     */
    default <T> T getValue(K key, CFunction<V, T> convert, T defaultValue) {

        val value = getValue(key);
        if(isInvalidValue(value)) {
            return defaultValue;
        }
        return ObjUtil.defaultIfNull(convert.apply(value), defaultValue);
    }

    /**
     * 获取 Integer 值
     * @param key key
     * @return Integer 值
     */
    default Integer getValueInt(K key){
        return CObjUtils.convert(getValue(key), Integer.class);
    }

    /**
     * 获取 Integer 值
     * @param key key
     * @return Integer 值 Optional
     */
    default Opt<Integer> getValueIntOpt(K key){
        return Opt.ofNullable(getValueInt(key));
    }

    /**
     * 获取 Long 值
     * @param key key
     * @return Long 值
     */
    default Long getValueLong(K key){
        return CObjUtils.convert(getValue(key), Long.class);
    }

    /**
     * 获取 Long 值
     * @param key key
     * @return Long 值 Optional
     */
    default Opt<Long> getValueLongOpt(K key){
        return Opt.ofNullable(getValueLong(key));
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

    /**
     * 获取值与剩余生存时间
     * @param key key
     * @return 值与剩余生存时间
     */
    default CValueWithTtl<V> getValueWithTtl(K key) {
        return getValueWithTtl(key, CFunction.self());
    }

    /**
     * 获取值与剩余生存时间
     * @param key key
     * @param convert 转换函数
     * @param <T> 转换类型
     * @return 值与剩余生存时间
     */
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
                value = CObjUtils.convert(valueInRedis, convert);
            }

            return new CValueWithTtl<>(value, ttl);
        });

    }

    /**
     * 自增 1
     * @param key key
     * @return 自增后的值
     */
    default Long incr(K key) {
        return opsForValue()
            .increment(key);
    }

    /**
     * 自增指定值
     * @param key key
     * @param delta 增量
     * @return 自增后的值
     */
    default Long incr(K key, long delta) {
        return opsForValue()
            .increment(key, delta);
    }

}
