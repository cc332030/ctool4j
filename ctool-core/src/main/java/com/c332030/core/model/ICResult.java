package com.c332030.core.model;

import com.c332030.core.log.JsonLog;

/**
 * <p>
 * Description: ICResult
 * </p>
 *
 * @since 2025/2/24
 */
@JsonLog
public interface ICResult<C, T> {

    C getCode();

    String getMessage();

    T getData();

}
