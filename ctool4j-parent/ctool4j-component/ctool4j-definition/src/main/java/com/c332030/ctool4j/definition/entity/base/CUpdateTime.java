package com.c332030.ctool4j.definition.entity.base;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

/**
 * <p>
 * Description: CCreateTime
 * </p>
 *
 * @since 2025/5/26
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CUpdateTime extends CCreateTime implements ICUpdateTime {

    @TableField(
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER
    )
    Date updateTime;

}
