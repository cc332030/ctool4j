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
 * @since 2025/12/6
 * @see doc/design/core/CBaseEntity.adoc
 * @see doc/design/core/CBaseEntityTests.adoc
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
