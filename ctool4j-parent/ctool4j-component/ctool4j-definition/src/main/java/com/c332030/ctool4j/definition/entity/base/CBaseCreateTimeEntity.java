package com.c332030.ctool4j.definition.entity.base;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * Description: CBaseCreateTimeEntity
 * </p>
 * <p>mybatis-plus 注解常量编译期无法解析：definition 模块不绑定 mybatis-plus 版本（provided，
 * 由使用方决定，多版本兼容），javac 的"未知枚举常量"警告已知且接受</p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CBaseCreateTimeEntity&lt;ID&gt;} 为基础创建时间实体，继承 {@code CId&lt;ID&gt;} 并实现 {@code ICCreateTime}，在 ID 基础上增加 {@code createTime} 字段。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要主键 + 创建时间的实体基类。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>createTime 不可插入/更新（strategy NEVER）。</li>
 * </ul>
 *
 * @since 2025/5/26
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CBaseCreateTimeEntity<ID extends Serializable> extends CId<ID> implements ICCreateTime {

    @TableField(
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER
    )
    Date createTime;

}
