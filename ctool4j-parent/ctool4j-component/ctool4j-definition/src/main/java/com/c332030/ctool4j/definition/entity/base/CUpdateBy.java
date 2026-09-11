package com.c332030.ctool4j.definition.entity.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: CUpdateBy
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CUpdateBy} 为更新人实体，继承 {@code CCreateBy} 并实现 {@code ICCreateUpdateBy}，在创建人基础上增加 {@code updateById} 与 {@code updateBy} 字段。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要创建/更新人字段的实体。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>创建人字段不可更新；更新人字段可更新。</li>
 * </ul>
 *
 * @since 2025/12/6
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CUpdateBy extends CCreateBy implements ICCreateUpdateBy {

    Long updateById;

    String updateBy;

}
