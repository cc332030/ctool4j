package com.c332030.ctool4j.web.cors.advice;

import com.c332030.ctool4j.web.advice.ICBaseResponseBodyAdvice;
import com.c332030.ctool4j.web.cors.util.CCorsUtils;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Description: CCorsResponseBodyAdvice
 * </p>
 *
 * @since 2025/11/12
 */
@CustomLog
//@ControllerAdvice
@AllArgsConstructor
public class CCorsResponseBodyAdvice implements ICBaseResponseBodyAdvice<Object> {

    @Nullable
    @Override
    public Object beforeBodyWrite(
        @Nullable Object body,
        MethodParameter returnType,
        MediaType selectedContentType,
        Class<? extends HttpMessageConverter<?>> selectedConverterType,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        CCorsUtils.handle(request, response);
        return body;
    }

}
