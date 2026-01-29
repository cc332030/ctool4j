package com.c332030.ctool4j.definition.model.result.impl;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.definition.model.result.ICIntResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;

/**
 * <p>
 * Description: CIntMsgResult
 * </p>
 *
 * @since 2025/5/13
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CIntResult<DATA> implements ICIntResult<DATA> {

    Integer code;

    String message;

    DATA data;

    public static <DATA> CIntResult<DATA> newInstance(Integer code, String message, DATA data) {
        return CIntResult.<DATA>builder()
            .code(code)
            .message(message)
            .data(data)
            .build();
    }

    public static <DATA> CIntResult<DATA> success() {
        return success(null);
    }

    public static <DATA> CIntResult<DATA> success(HttpStatus httpStatus, DATA data) {
        return newInstance(
            httpStatus.value(),
            httpStatus.getReasonPhrase(),
            data
        );
    }

    public static <DATA> CIntResult<DATA> success(DATA data) {
        return success(
            HttpStatus.OK,
            data
        );
    }

    public static <DATA> CIntResult<DATA> error(Integer code, String message) {
        return newInstance(code, message, null);
    }

    public static <DATA> CIntResult<DATA> error(HttpStatus httpStatus, String message) {
        return error(
            httpStatus.value(),
            StrUtil.nullToDefault(message, httpStatus.getReasonPhrase())
        );
    }

    public static <DATA> CIntResult<DATA> error(HttpStatus httpStatus) {
        return error(
            httpStatus,
            null
        );
    }

    public static <DATA> CIntResult<DATA> error(String message) {
        return error(
            HttpStatus.INTERNAL_SERVER_ERROR,
            message
        );
    }

}
