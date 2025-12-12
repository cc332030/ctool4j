package com.c332030.ctool4j.web.advice;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Description: ICBaseResponseBodyAdvice
 * </p>
 *
 * @since 2025/9/28
 */
public interface ICResponseBodyAdvice<T> extends ICBaseResponseBodyAdvice<T> {

    @Override
    default T beforeBodyWrite(
        T body,
        MethodParameter returnType,
        MediaType selectedContentType,
        Class<? extends HttpMessageConverter<?>> selectedConverterType,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        return handleBody(body);
    }

    default T handleBody(T body, HttpServletRequest request, HttpServletResponse response) {
        return handleBody(body);
    }

    default T handleBody(T body) {
        return body;
    }

}
