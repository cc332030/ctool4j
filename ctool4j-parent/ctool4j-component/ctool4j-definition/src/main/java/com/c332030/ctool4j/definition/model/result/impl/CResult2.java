package com.c332030.ctool4j.definition.model.result.impl;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.definition.model.ICResult;
import lombok.AllArgsConstructor;
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
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CResult2<DATA> implements ICResult<String, DATA> {

    String code;

    String message;

    DATA data;

    public static <DATA> CResult2<DATA> newInstance(String code, String message, DATA data) {
        return CResult2.<DATA>builder()
                .code(code)
                .message(message)
                .data(data)
                .build();
    }

    public static <DATA> CResult2<DATA> success() {
        return success(null);
    }

    public static <DATA> CResult2<DATA> success(DATA data) {
        return newInstance("000000", HttpStatus.OK.getReasonPhrase(), data);
    }

    public static <DATA> CResult2<DATA> error(String message) {
        return error(
                "999999",
                StrUtil.nullToDefault(message, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
        );
    }

    public static <DATA> CResult2<DATA> error(String code, String message) {
        return newInstance(code, message, null);
    }

}
