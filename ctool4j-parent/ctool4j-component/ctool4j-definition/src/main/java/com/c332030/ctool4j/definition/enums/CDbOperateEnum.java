package com.c332030.ctool4j.definition.enums;

import com.c332030.ctool4j.definition.interfaces.ICOperate;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Description: CDbOperateEnum
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CDbOperateEnum} 为数据库操作枚举，实现 {@code ICOperate}，定义四种操作（INSERT/SELECT/UPDATE/DELETE）， 各带中文描述 {@code text}，提供 {@code getText()} 与 {@code getName()}。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>数据库操作类型的标准化枚举。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>描述为中文文本。</li>
 * </ul>
 *
 * @since 2025/11/10
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum CDbOperateEnum implements ICOperate {

    INSERT("插入"),

    SELECT("查询"),

    UPDATE("更新"),

    DELETE("删除"),

    ;

    /**
     * 描述
     */
    final String text;

}
