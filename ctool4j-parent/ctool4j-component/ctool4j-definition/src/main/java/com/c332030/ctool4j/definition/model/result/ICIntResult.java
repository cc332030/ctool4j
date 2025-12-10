package com.c332030.ctool4j.definition.model.result;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.definition.model.result.impl.CIntResult;
import org.springframework.http.HttpStatus;

/**
 * <p>
 * Description: ICResult
 * </p>
 *
 * @since 2025/2/24
 */
public interface ICIntResult<DATA> extends ICResult<Integer, DATA> {

    static <DATA> CIntResult<DATA> newInstance(Integer code, String message, DATA data) {
        return CIntResult.<DATA>builder()
            .code(code)
            .msg(message)
            .data(data)
            .build();
    }

    static <DATA> CIntResult<DATA> success() {
        return success(null);
    }

    static <DATA> CIntResult<DATA> success(DATA data) {
        return newInstance(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), data);
    }

    static <DATA> CIntResult<DATA> error(String message) {
        return error(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            StrUtil.nullToDefault(message, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
        );
    }

    static <DATA> CIntResult<DATA> error(Integer code, String message) {
        return newInstance(code, message, null);
    }

}
