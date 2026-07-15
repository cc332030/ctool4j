package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.exception.annotation.ConditionalOnMissingExceptionHandler;
import lombok.CustomLog;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.reflect.UndeclaredThrowableException;

/**
 * <p>
 * Description: CUndeclaredThrowableExceptionHandler
 * </p>
 *
 * @since 2026/4/9
 */
@CustomLog
@RestControllerAdvice
@ConditionalOnMissingExceptionHandler(UndeclaredThrowableException.class)
public class CUndeclaredThrowableExceptionHandler {

    @ExceptionHandler(UndeclaredThrowableException.class)
    public CStrResult<Void> handle(UndeclaredThrowableException e) {

        log.error("handle UndeclaredThrowableException，requestURI: {}", CRequestUtils.getRequestURIDefaultNull(), e.getCause());
        return CStrResult.error("未知异常");
    }

}
