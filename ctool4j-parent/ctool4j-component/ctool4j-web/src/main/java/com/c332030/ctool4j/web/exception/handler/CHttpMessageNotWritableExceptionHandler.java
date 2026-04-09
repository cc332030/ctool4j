package com.c332030.ctool4j.web.exception.handler;

import lombok.CustomLog;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * Description: CExceptionHandler
 * </p>
 *
 * @since 2026/4/9
 */
@CustomLog
@RestControllerAdvice
@Conditional(CHttpMessageNotWritableExceptionHandler.Condition.class)
public class CHttpMessageNotWritableExceptionHandler {

    public static class Condition extends CAbstractMissingExceptionHandlerCondition {
        public Condition() {
            super(HttpMessageNotWritableException.class);
        }
    }

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public void handle(HttpMessageNotWritableException e) {
        log.debug("handle HttpMessageNotWritableException", e);
    }

}
