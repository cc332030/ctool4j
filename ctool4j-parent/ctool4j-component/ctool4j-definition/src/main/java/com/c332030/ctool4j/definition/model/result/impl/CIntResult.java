package com.c332030.ctool4j.definition.model.result.impl;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.definition.model.result.ICIntResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

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

    /**
     * 构造结果
     *
     * @param code    状态码
     * @param message 消息
     * @param data    数据
     * @param <DATA>  数据类型
     * @return 结果
     */
    public static <DATA> CIntResult<DATA> newInstance(Integer code, String message, DATA data) {
        return CIntResult.<DATA>builder()
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
    public static <DATA> CIntResult<DATA> success() {
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
    public static <DATA> CIntResult<DATA> success(@NonNull HttpStatus httpStatus, DATA data) {
        return newInstance(
            httpStatus.value(),
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
    public static <DATA> CIntResult<DATA> success(DATA data) {
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
    public static <DATA> CIntResult<DATA> error(Integer code, String message) {
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
    public static <DATA> CIntResult<DATA> error(@NonNull HttpStatus httpStatus, String message) {
        return error(
            httpStatus.value(),
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
    public static <DATA> CIntResult<DATA> error(@NonNull HttpStatus httpStatus) {
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
    public static <DATA> CIntResult<DATA> error(String message) {
        return error(
            HttpStatus.INTERNAL_SERVER_ERROR,
            message
        );
    }

}
