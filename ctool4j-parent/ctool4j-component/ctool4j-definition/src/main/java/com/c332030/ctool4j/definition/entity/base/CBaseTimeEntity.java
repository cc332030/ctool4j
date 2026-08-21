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
 * Description: CBaseTimeEntity
 * </p>
 * <p>mybatis-plus 注解常量编译期无法解析：definition 模块不绑定 mybatis-plus 版本（provided，
 * 由使用方决定，多版本兼容），javac 的"未知枚举常量"警告已知且接受</p>
 *
 * @since 2025/5/26
 * @see "doc/design/core/CBaseTimeEntity.adoc"
 * @see "doc/design/core/CBaseTimeEntityTests.adoc"
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CBaseTimeEntity<ID extends Serializable> extends CBaseCreateTimeEntity<ID> implements ICCreateUpdateTime {

    @TableField(
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER
    )
    Date updateTime;

}
