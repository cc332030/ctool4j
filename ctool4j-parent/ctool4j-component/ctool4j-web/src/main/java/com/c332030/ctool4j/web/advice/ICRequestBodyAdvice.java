package com.c332030.ctool4j.web.advice;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * <p>
 * Description: CRequestBodyAdvice
 * </p>
 *
 * @since 2025/9/28
 */
public interface ICRequestBodyAdvice extends RequestBodyAdvice {

    @Override
    default boolean supports(
            @NonNull MethodParameter methodParameter,
            @NonNull Type targetType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return true;
    }

    @NonNull
    @Override
    default HttpInputMessage beforeBodyRead(
            @NonNull HttpInputMessage inputMessage,
            @NonNull MethodParameter parameter,
            @NonNull Type targetType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType
    ) throws IOException {
        return inputMessage;
    }

    @NonNull
    @Override
    default Object afterBodyRead(
            @NonNull Object body,
            @NonNull HttpInputMessage inputMessage,
            @NonNull MethodParameter parameter,
            @NonNull Type targetType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return body;
    }

    @NonNull
    @Override
    default Object handleEmptyBody(
            Object body,
            @NonNull HttpInputMessage inputMessage,
            @NonNull MethodParameter parameter,
            @NonNull Type targetType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return body;
    }
}
