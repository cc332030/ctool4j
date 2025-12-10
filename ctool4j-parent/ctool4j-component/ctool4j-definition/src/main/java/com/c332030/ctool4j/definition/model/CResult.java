package com.c332030.ctool4j.definition.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: CResult
 * </p>
 *
 * @since 2025/5/13
 */
@Deprecated
@Data
@SuperBuilder
@NoArgsConstructor
public class CResult<DATA> extends com.c332030.ctool4j.definition.model.result.impl.CResult<DATA> implements ICResult<Integer, DATA> {

}
