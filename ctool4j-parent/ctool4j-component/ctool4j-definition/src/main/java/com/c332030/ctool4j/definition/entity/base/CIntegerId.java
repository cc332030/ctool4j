package com.c332030.ctool4j.definition.entity.base;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: CIntegerId
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CIntegerId} 为 Integer 类型 ID 实体，继承 {@code CId&lt;Integer&gt;} 并实现 {@code ICIntegerId}，固定主键为 Integer 类型。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>主键为 Integer 的实体基类。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>固定 ID 类型为 Integer。</li>
 * </ul>
 *
 * @since 2025/5/26
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class CIntegerId extends CId<Integer> implements ICIntegerId {

}
