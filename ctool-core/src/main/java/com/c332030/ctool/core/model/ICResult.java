package com.c332030.ctool.core.model;

import com.c332030.ctool.core.log.CJsonLog;

/**
 * <p>
 * Description: ICResult
 * </p>
 *
 * @since 2025/2/24
 */
@CJsonLog
public interface ICResult<C, T> {

    C getCode();

    String getMessage();

    T getData();

}
