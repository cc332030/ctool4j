package com.c332030.ctool4j.definition.model;

import com.c332030.ctool4j.definition.model.result.impl.CIntResult;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: CIntResult
 * </p>
 *
 * @since 2025/5/13
 */
@Deprecated
@Data
@SuperBuilder
@NoArgsConstructor
public class CResult<DATA> extends CIntResult<DATA> implements ICResult<Integer, DATA> {

}
