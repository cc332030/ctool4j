package com.c332030.ctool4j.definition.model;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.definition.model.result.impl.CCodeMessageDataResult;
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
public class CResult2<T> extends CCodeMessageDataResult<String, T> implements ICResult<String, T> {

    public static <T> CResult2<T> newInstance(String code, String message, T data) {
        return CResult2.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> CResult2<T> success() {
        return success(null);
    }

    public static <T> CResult2<T> success(T data) {
        return newInstance("000000", HttpStatus.OK.getReasonPhrase(), data);
    }

    public static <T> CResult2<T> error(String message) {
        return error(
                "999999",
                StrUtil.nullToDefault(message, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
        );
    }

    public static <T> CResult2<T> error(String code, String message) {
        return newInstance(code, message, null);
    }

}
