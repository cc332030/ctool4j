package com.c332030.ctool4j.definition.model.result;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.definition.model.result.impl.CIntMsgResult;
import org.springframework.http.HttpStatus;

/**
 * <p>
 * Description: ICResult
 * </p>
 *
 * @since 2025/2/24
 */
public interface ICIntMsgResult<DATA> extends ICResult<Integer, DATA>, ICCodeMsgDataResult<Integer, DATA> {

    static <DATA> CIntMsgResult<DATA> newInstance(Integer code, String message, DATA data) {
        return CIntMsgResult.<DATA>builder()
            .code(code)
            .msg(message)
            .data(data)
            .build();
    }

    static <DATA> CIntMsgResult<DATA> success() {
        return success(null);
    }

    static <DATA> CIntMsgResult<DATA> success(DATA data) {
        return newInstance(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), data);
    }

    static <DATA> CIntMsgResult<DATA> error(String message) {
        return error(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            StrUtil.nullToDefault(message, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
        );
    }

    static <DATA> CIntMsgResult<DATA> error(Integer code, String message) {
        return newInstance(code, message, null);
    }

}
