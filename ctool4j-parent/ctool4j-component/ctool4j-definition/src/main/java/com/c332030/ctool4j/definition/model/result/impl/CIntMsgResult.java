package com.c332030.ctool4j.definition.model.result.impl;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.definition.model.result.ICIntMsgResult;
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
 * @see "doc/design/core/CIntMsgResult.adoc"
 * @see "doc/design/core/CIntMsgResultTests.adoc"
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CIntMsgResult<DATA> implements ICIntResult<DATA>, ICIntMsgResult<DATA> {

    Integer code;

    String msg;

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
    public static <DATA> CIntMsgResult<DATA> newInstance(Integer code, String message, DATA data) {
        return CIntMsgResult.<DATA>builder()
                .code(code)
                .msg(message)
                .data(data)
                .build();
    }

    /**
     * 成功结果（无数据）
     *
     * @param <DATA> 数据类型
     * @return 成功结果
     */
    public static <DATA> CIntMsgResult<DATA> success() {
        return success(null);
    }

    /**
     * 成功结果
     *
     * @param data   数据
     * @param <DATA> 数据类型
     * @return 成功结果
     */
    public static <DATA> CIntMsgResult<DATA> success(DATA data) {
        return newInstance(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), data);
    }

    /**
     * 失败结果（默认 500）
     *
     * @param message 消息
     * @param <DATA>  数据类型
     * @return 失败结果
     */
    public static <DATA> CIntMsgResult<DATA> error(String message) {
        return error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                StrUtil.nullToDefault(message, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
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
    public static <DATA> CIntMsgResult<DATA> error(Integer code, String message) {
        return newInstance(code, message, null);
    }

}
