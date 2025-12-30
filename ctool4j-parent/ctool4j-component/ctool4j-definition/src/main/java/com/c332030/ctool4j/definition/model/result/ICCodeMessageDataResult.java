package com.c332030.ctool4j.definition.model.result;

import com.c332030.ctool4j.definition.annotation.CJsonLog;
import com.c332030.ctool4j.definition.interfaces.ICCode;
import com.c332030.ctool4j.definition.interfaces.ICData;
import com.c332030.ctool4j.definition.interfaces.ICMessage;

/**
 * <p>
 * Description: ICCodeMsgDataResult
 * </p>
 *
 * @author c332030
 * @since 2025/12/9
 */
@CJsonLog
public interface ICCodeMessageDataResult<CODE, DATA> extends ICCode<CODE>, ICMessage, ICData<DATA> {

}
