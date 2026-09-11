package com.c332030.ctool4j.definition.entity.base;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * <p>
 * Description: CBaseEntity
 * </p>
 * <p>mybatis-plus 注解常量编译期无法解析：definition 模块不绑定 mybatis-plus 版本（provided，
 * 由使用方决定，多版本兼容），javac 的"未知枚举常量"警告已知且接受</p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CBaseEntity&lt;ID&gt;} 为完整基础实体，继承 {@code CBaseTimeEntity&lt;ID&gt;} 并实现 {@code ICCreateUpdateByAndTime}， 在时间字段基础上增加 {@code createById}/{@code createBy}/{@code updateById}/{@code updateBy} 创建人/更新人字段。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要主键 + 时间 + 创建/更新人完整审计字段的实体基类。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>创建人字段不可更新（strategy NEVER）；更新人字段可更新。</li>
 * </ul>
 *
 * @since 2025/12/6
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CBaseEntity<ID extends Serializable> extends CBaseTimeEntity<ID> implements ICCreateUpdateByAndTime {

    @TableField(updateStrategy = FieldStrategy.NEVER)
    Long createById;

    @TableField(updateStrategy = FieldStrategy.NEVER)
    String createBy;

    Long updateById;

    String updateBy;

}
