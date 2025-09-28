package com.c332030.ctool4j.web.advice;

import com.c332030.ctool4j.core.util.CSpiUtils;
import com.c332030.ctool4j.web.advice.handler.ICResponseBeforeBodyWriteHandler;
import lombok.CustomLog;
import lombok.val;
import lombok.var;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * Description: CResponseBodyAdvice
 * </p>
 *
 * @since 2025/9/28
 */
@CustomLog
@ControllerAdvice
public class CResponseBodyAdvice extends CBaseResponseBodyAdvice<Object> {

    final List<ICResponseBeforeBodyWriteHandler> handlers =
            CSpiUtils.getImplsSorted(ICResponseBeforeBodyWriteHandler.class);

    @Override
    Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        var newBody = body;
        for (val handler : handlers) {
            try {
                newBody = handler.apply(newBody);
            } catch (Throwable e) {
                log.error("deal response body error", e);
            }
        }

        return newBody;
    }

}
