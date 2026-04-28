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
 *
 * @since 2025/12/6
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
