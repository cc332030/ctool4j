package com.c332030.ctool4j.definition.entity.base;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: CIntegerId
 * </p>
 *
 * @since 2025/5/26
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class CIntegerId extends CId<Integer> implements ICIntegerId {

}
