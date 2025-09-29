package com.c332030.ctool4j.web.advice;

import com.c332030.ctool4j.core.util.CSpiUtils;
import com.c332030.ctool4j.web.advice.handler.ICRequestAfterBodyReadHandler;
import lombok.CustomLog;
import lombok.val;
import lombok.var;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.lang.reflect.Type;
import java.util.List;

/**
 * <p>
 * Description: CRequestBodyAdvice
 * </p>
 *
 * @since 2025/9/28
 */
@CustomLog
@ControllerAdvice
public class CRequestBodyAdvice extends CBaseRequestBodyAdvice {

    final List<ICRequestAfterBodyReadHandler> handlers =
            CSpiUtils.getImplsSorted(ICRequestAfterBodyReadHandler.class);

    @NonNull
    @Override
    public Object afterBodyRead(
            @NonNull Object body,
            @NonNull HttpInputMessage inputMessage,
            @NonNull MethodParameter parameter,
            @NonNull Type targetType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType
    ) {

        var newBody = body;
        for (val handler : handlers) {
            try {
                newBody = handler.apply(newBody);
            } catch (Throwable e) {
                log.error("deal request body error with handler: {}", handler, e);
            }
        }

        return newBody;
    }

}
