package com.c332030.ctool4j.definition.model.result;

import com.c332030.ctool4j.definition.annotation.CJsonLog;
import com.c332030.ctool4j.definition.interfaces.ICCode;
import com.c332030.ctool4j.definition.interfaces.ICData;
import com.c332030.ctool4j.definition.interfaces.ICMessage;

/**
 * <p>
 * Description: ICCodeMessageDataResult
 * </p>
 *
 * @author c332030
 * @see doc/design/definition/ICCodeMessageDataResult.adoc
 * @since 2025/12/9
 */
@CJsonLog
public interface ICCodeMessageDataResult<CODE, DATA> extends ICCode<CODE>, ICMessage, ICData<DATA> {

}
