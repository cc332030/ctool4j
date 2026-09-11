package com.c332030.ctool4j.redis.model;

import com.c332030.ctool4j.definition.interfaces.ICValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: CValueWithTtl
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CValueWithTtl&lt;T&gt;} 为携带值及其剩余存活时间的简单数据载体，实现 {@code ICValue&lt;T&gt;}。</p>
 * <ul>
 *   <li>{@code value}：缓存值</li>
 *   <li>{@code ttl}：剩余存活时间（秒）；{@code -1} 表示 key 存在但无过期时间，{@code -2} 表示 key 不存在</li>
 * </ul>
 * <p>由 lombok {@code @Data}/{@code @SuperBuilder}/{@code @NoArgsConstructor}/{@code @AllArgsConstructor} 生成。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>值不存在</td>
 *     <td>value 为 null，ttl 反映 Redis TTL（-2 等）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要同时读取缓存值与其剩余过期时间的场景（如按 TTL 分流刷新）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>仅承载数据，无业务逻辑；不校验 value/ttl 的一致性。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>{@code ttl} 数值语义依赖调用方对 Redis TTL 约定的理解，本类不解释。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义约定</b></p>
 * <ul>
 *   <li>{@code ttl} 单位固定为秒，语义与 Redis TTL 命令一致。</li>
 *   <li>泛型 {@code T} 承载值的类型。</li>
 * </ul>
 * <p><b>实现方式</b></p>
 * <ul>
 *   <li>纯数据类，配合 {@code ICRedisService.getValueWithTtl} 返回值与剩余 TTL。</li>
 * </ul>
 *
 * @since 2026/3/25
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CValueWithTtl<T> implements ICValue<T> {

    T value;

    Long ttl;

}
