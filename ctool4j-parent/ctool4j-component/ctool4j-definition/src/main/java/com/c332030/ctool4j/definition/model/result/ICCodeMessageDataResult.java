package com.c332030.ctool4j.definition.model.result;

import com.c332030.ctool4j.definition.annotation.CJsonLog;

/**
 * <p>
 * Description: ICCodeMsgDataResult
 * </p>
 *
 * @author c332030
 * @since 2025/12/9
 */
@CJsonLog
public interface ICCodeMessageDataResult<CODE, DATA> extends ICMessage {

    CODE getCode();

    String getMessage();

    DATA getData();

}
