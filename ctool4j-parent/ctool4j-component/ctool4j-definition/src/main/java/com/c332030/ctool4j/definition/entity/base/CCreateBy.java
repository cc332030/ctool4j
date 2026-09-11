package com.c332030.ctool4j.definition.entity.base;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: CCreateBy
 * </p>
 * <p>mybatis-plus 注解常量编译期无法解析：definition 模块不绑定 mybatis-plus 版本（provided，
 * 由使用方决定，多版本兼容），javac 的"未知枚举常量"警告已知且接受</p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CCreateBy} 为创建人实体，实现 {@code ICCreateBy}，含 {@code createById} 与 {@code createBy} 字段（@TableField 不可更新）。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要创建人字段的实体。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>创建人字段不可更新。</li>
 * </ul>
 *
 * @since 2025/12/6
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CCreateBy implements ICCreateBy {

    @TableField(
            updateStrategy = FieldStrategy.NEVER
    )
    Long createById;

    @TableField(
            updateStrategy = FieldStrategy.NEVER
    )
    String createBy;

}
