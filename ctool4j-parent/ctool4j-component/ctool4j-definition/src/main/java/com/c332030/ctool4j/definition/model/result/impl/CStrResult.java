package com.c332030.ctool4j.definition.model.result.impl;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.definition.model.result.ICStrResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

/**
 * <p>
 * Description: CStrResult
 * </p>
 *
 * @since 2025/5/13
 * @see doc/design/core/CStrResult.adoc
 * @see doc/design/core/CStrResultTests.adoc
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CStrResult<DATA> implements ICStrResult<DATA> {

    String code;

    String message;

    DATA data;

    /**
     * 构造结果
     *
     * @param code    状态码
     * @param message 消息
     * @param data    数据
     * @param <DATA>  数据类型
     * @return 结果
     */
    public static <DATA> CStrResult<DATA> newInstance(String code, String message, DATA data) {
        return CStrResult.<DATA>builder()
            .code(code)
            .message(message)
            .data(data)
            .build();
    }

    /**
     * 成功结果（无数据）
     *
     * @param <DATA> 数据类型
     * @return 成功结果
     */
    public static <DATA> CStrResult<DATA> success() {
        return success(null);
    }

    /**
     * 成功结果（指定状态）
     *
     * @param httpStatus 状态
     * @param data       数据
     * @param <DATA>     数据类型
     * @return 成功结果
     */
    public static <DATA> CStrResult<DATA> success(@NonNull HttpStatus httpStatus, DATA data) {
        return newInstance(
            String.valueOf(httpStatus.value()),
            httpStatus.getReasonPhrase(),
            data
        );
    }

    /**
     * 成功结果
     *
     * @param data   数据
     * @param <DATA> 数据类型
     * @return 成功结果
     */
    public static <DATA> CStrResult<DATA> success(DATA data) {
        return success(
            HttpStatus.OK,
            data
        );
    }

    /**
     * 失败结果
     *
     * @param code    状态码
     * @param message 消息
     * @param <DATA>  数据类型
     * @return 失败结果
     */
    public static <DATA> CStrResult<DATA> error(String code, String message) {
        return newInstance(code, message, null);
    }

    /**
     * 失败结果（指定状态）
     *
     * @param httpStatus 状态
     * @param message    消息
     * @param <DATA>     数据类型
     * @return 失败结果
     */
    public static <DATA> CStrResult<DATA> error(@NonNull HttpStatus httpStatus, String message) {
        return error(
            String.valueOf(httpStatus.value()),
            StrUtil.nullToDefault(message, httpStatus.getReasonPhrase())
        );
    }

    /**
     * 失败结果（仅状态）
     *
     * @param httpStatus 状态
     * @param <DATA>     数据类型
     * @return 失败结果
     */
    public static <DATA> CStrResult<DATA> error(@NonNull HttpStatus httpStatus) {
        return error(
            httpStatus,
            null
        );
    }

    /**
     * 失败结果（默认 500）
     *
     * @param message 消息
     * @param <DATA>  数据类型
     * @return 失败结果
     */
    public static <DATA> CStrResult<DATA> error(String message) {
        return error(
            HttpStatus.INTERNAL_SERVER_ERROR,
            message
        );
    }

}
