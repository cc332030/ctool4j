package com.c332030.ctool4j.definition.model;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.definition.model.result.ICIntMsgResult;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;

/**
 * <p>
 * Description: CResult
 * </p>
 *
 * @since 2025/5/13
 */
@Deprecated
@Data
@SuperBuilder
@NoArgsConstructor
public class CResult<DATA> implements ICIntMsgResult<DATA> {

    Integer code;

    String msg;

    DATA data;

    public static <DATA> CResult<DATA> newInstance(Integer code, String message, DATA data) {
        return CResult.<DATA>builder()
            .code(code)
            .msg(message)
            .data(data)
            .build();
    }

    public static <DATA> CResult<DATA> success() {
        return success(null);
    }

    public static <DATA> CResult<DATA> success(DATA data) {
        return newInstance(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), data);
    }

    public static <DATA> CResult<DATA> error(String message) {
        return error(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            StrUtil.nullToDefault(message, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
        );
    }

    public static <DATA> CResult<DATA> error(Integer code, String message) {
        return newInstance(code, message, null);
    }

}
