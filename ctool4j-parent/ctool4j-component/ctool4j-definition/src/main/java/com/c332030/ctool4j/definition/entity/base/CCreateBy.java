package com.c332030.ctool4j.definition.entity.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: CCreateBy
 * </p>
 *
 * @since 2025/12/6
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CCreateBy implements ICCreateBy {

    Long createById;

    String createBy;

}
