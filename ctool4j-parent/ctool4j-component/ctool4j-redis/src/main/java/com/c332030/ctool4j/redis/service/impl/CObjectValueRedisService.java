package com.c332030.ctool4j.redis.service.impl;

import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.redis.service.ICRedisService;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Description: CObjectValueRedisService
 * </p>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>key 无效</td>
 *     <td>由 {@code ICRedisService.isInvalidKey}（Objects.isNull）判定，getValue 返回 null</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>key/value 为任意 Object 的 Redis 读写，如对象缓存。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>key/value 需精确 String 类型时使用 {@code CStringStringRedisService}。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>{@code getValueForGenericType} 依赖 anyType 强转，调用方需保证返回类型正确，否则运行期可能 ClassCastException。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义约定</b></p>
 * <ul>
 *   <li>RedisTemplate 声明为 {@code RedisTemplate&lt;? super String, Object&gt;}，兼容 String 和 Object key。</li>
 *   <li>{@code getValueForGenericType} 用 {@code CObjUtils.anyType} 做泛型强转。</li>
 * </ul>
 * <p><b>实现方式</b></p>
 * <ul>
 *   <li>构造器注入 {@code RedisTemplate&lt;? super String, Object&gt;}；{@code getValueForGenericType} 返回 anyType(getValue(key))。</li>
 * </ul>
 *
 * @since 2025/11/4
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class CObjectValueRedisService implements ICRedisService<Object, Object> {

    /**
     * RedisTemplate ，兼容 String 和 Object 类型的 key
     * 有的项目 key 全是 String，兼容 String 类型的 key
     */
    RedisTemplate<? super String, Object> redisTemplate;

    /**
     * 获取 RedisTemplate
     * <ul>
     *   <li>{@code getRedisTemplate()}：返回泛型 {@code RedisTemplate&lt;Object, Object&gt;}。</li>
     * </ul>
     *
     * @return RedisTemplate 通用
     */
    @Override
    public RedisTemplate<Object, Object> getRedisTemplate() {
        return CObjUtils.anyType(redisTemplate);
    }

    /**
     * 按泛型类型获取值
     * <ul>
     *   <li>{@code getValueForGenericType(key)}：按泛型类型获取值。</li>
     * </ul>
     *
     * @param key key
     * @param <T> 返回类型
     * @return 值，转换为目标泛型类型
     */
    public <T> T getValueForGenericType(Object key) {
        return CObjUtils.anyType(getValue(key));
    }

}
