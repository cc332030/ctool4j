package com.c332030.ctool4j.web.advice;

import lombok.val;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Description: ICBaseResponseBodyAdvice
 * </p>
 *
 * @since 2025/9/28
 */
public interface ICBaseResponseBodyAdvice<T> extends ResponseBodyAdvice<T> {

    @Override
    default boolean supports(
            @NonNull MethodParameter returnType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return true;
    }

    @Override
    default T beforeBodyWrite(
            T body,
            @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response
    ) {

        val httpServletRequest = ((ServletServerHttpRequest)request).getServletRequest();
        val httpServletResponse = ((ServletServerHttpResponse)response).getServletResponse();
        return beforeBodyWrite(
                body, returnType, selectedContentType, selectedConverterType,
                httpServletRequest,
                httpServletResponse
        );
    }

    T beforeBodyWrite(
            T body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            HttpServletRequest request,
            HttpServletResponse response
    );

}
