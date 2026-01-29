package com.c332030.ctool4j.definition.model.result.impl;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.definition.model.result.ICStrResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;

/**
 * <p>
 * Description: CStrResult
 * </p>
 *
 * @since 2025/5/13
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CStrResult<DATA> implements ICStrResult<DATA> {

    String code;

    String message;

    DATA data;

    public static <DATA> CStrResult<DATA> newInstance(String code, String message, DATA data) {
        return CStrResult.<DATA>builder()
            .code(code)
            .message(message)
            .data(data)
            .build();
    }

    public static <DATA> CStrResult<DATA> success() {
        return success(null);
    }

    public static <DATA> CStrResult<DATA> success(HttpStatus httpStatus, DATA data) {
        return newInstance(
            String.valueOf(httpStatus.value()),
            httpStatus.getReasonPhrase(),
            data
        );
    }

    public static <DATA> CStrResult<DATA> success(DATA data) {
        return success(
            HttpStatus.OK,
            data
        );
    }

    public static <DATA> CStrResult<DATA> error(String code, String message) {
        return newInstance(code, message, null);
    }

    public static <DATA> CStrResult<DATA> error(HttpStatus httpStatus, String message) {
        return error(
            String.valueOf(httpStatus.value()),
            StrUtil.nullToDefault(message, httpStatus.getReasonPhrase())
        );
    }

    public static <DATA> CStrResult<DATA> error(HttpStatus httpStatus) {
        return error(
            httpStatus,
            null
        );
    }

    public static <DATA> CStrResult<DATA> error(String message) {
        return error(
            HttpStatus.INTERNAL_SERVER_ERROR,
            message
        );
    }

}
