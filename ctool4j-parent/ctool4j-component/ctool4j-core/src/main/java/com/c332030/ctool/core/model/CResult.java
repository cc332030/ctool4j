package com.c332030.ctool.core.model;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class CResult<T> implements ICResult<Integer, T> {

    Integer code;

    String msg;

    T data;

    @JsonIgnore
    @Override
    public String getMessage() {
        return getMsg();
    }

    public static <T> CResult<T> newInstance(Integer code, String msg, T data) {
        return CResult.<T>builder()
                .code(code)
                .msg(msg)
                .data(data)
                .build();
    }

    public static <T> CResult<T> success() {
        return success(null);
    }

    public static <T> CResult<T> success(T data) {
        return newInstance(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), data);
    }

    public static <T> CResult<T> error(String message) {
        return error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                StrUtil.nullToDefault(message, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
        );
    }

    public static <T> CResult<T> error(Integer code, String message) {
        return newInstance(code, message, null);
    }

}
