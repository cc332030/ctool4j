package com.c332030.ctool4j.definition.model.result;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import org.springframework.http.HttpStatus;

/**
 * <p>
 * Description: ICResult
 * </p>
 *
 * @since 2025/2/24
 */
public interface ICStrResult<DATA> extends ICResult<String, DATA> {

    static <DATA> CStrResult<DATA> newInstance(String code, String message, DATA data) {
        return CStrResult.<DATA>builder()
            .code(code)
            .message(message)
            .data(data)
            .build();
    }

    static <DATA> CStrResult<DATA> success() {
        return success(null);
    }

    static <DATA> CStrResult<DATA> success(DATA data) {
        return newInstance("000000", HttpStatus.OK.getReasonPhrase(), data);
    }

    static <DATA> CStrResult<DATA> error(String message) {
        return error(
            "999999",
            StrUtil.nullToDefault(message, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
        );
    }

    static <DATA> CStrResult<DATA> error(String code, String message) {
        return newInstance(code, message, null);
    }

}
