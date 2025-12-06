package com.c332030.ctool4j.definition.entity.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

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
public class CBaseEntity extends CBaseTimeEntity implements ICUpdateBy{

    Long createById;

    String createBy;

    Long updateById;

    String updateBy;

}
