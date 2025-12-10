package com.c332030.ctool4j.definition.model.result.impl;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.definition.model.result.ICCodeMsgDataResult;
import com.c332030.ctool4j.definition.model.result.ICIntResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;

/**
 * <p>
 * Description: CIntResult
 * </p>
 *
 * @since 2025/5/13
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CIntResult<DATA> implements ICIntResult<DATA>, ICCodeMsgDataResult<Integer, DATA> {

    Integer code;

    String msg;

    DATA data;

    public static <DATA> CIntResult<DATA> newInstance(Integer code, String message, DATA data) {
        return CIntResult.<DATA>builder()
                .code(code)
                .msg(message)
                .data(data)
                .build();
    }

    public static <DATA> CIntResult<DATA> success() {
        return success(null);
    }

    public static <DATA> CIntResult<DATA> success(DATA data) {
        return newInstance(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), data);
    }

    public static <DATA> CIntResult<DATA> error(String message) {
        return error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                StrUtil.nullToDefault(message, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
        );
    }

    public static <DATA> CIntResult<DATA> error(Integer code, String message) {
        return newInstance(code, message, null);
    }

}
