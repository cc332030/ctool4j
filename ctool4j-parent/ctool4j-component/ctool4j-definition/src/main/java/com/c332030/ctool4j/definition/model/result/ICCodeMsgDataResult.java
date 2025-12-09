package com.c332030.ctool4j.definition.model.result;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * <p>
 * Description: ICCodeMsgDataResult
 * </p>
 *
 * @author c332030
 * @since 2025/12/9
 */
public interface ICCodeMsgDataResult<CODE, DATA> extends ICBaseResult<CODE, DATA> {

    CODE getCode();

    String getMsg();

    DATA getData();

    @Override
    @JsonIgnore
    default String getMessage() {
        return getMsg();
    }

}
