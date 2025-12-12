package com.c332030.ctool4j.web.advice;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.NonNull;

import java.lang.reflect.Type;

/**
 * <p>
 * Description: ICRequestBodyAdvice
 * </p>
 *
 * @since 2025/12/12
 */
public interface ICRequestBodyAdvice extends ICBaseRequestBodyAdvice {

    @NonNull
    @Override
    default Object afterBodyRead(
        @NonNull Object body,
        @NonNull HttpInputMessage inputMessage,
        @NonNull MethodParameter parameter,
        @NonNull Type targetType,
        @NonNull Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return handleBody(body);
    }

    Object handleBody(Object body);

}
