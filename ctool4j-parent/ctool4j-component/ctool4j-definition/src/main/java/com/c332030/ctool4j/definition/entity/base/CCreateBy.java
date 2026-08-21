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
 * @since 2025/12/6
 * @see doc/design/core/CCreateBy.adoc
 * @see doc/design/core/CCreateByTests.adoc
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
