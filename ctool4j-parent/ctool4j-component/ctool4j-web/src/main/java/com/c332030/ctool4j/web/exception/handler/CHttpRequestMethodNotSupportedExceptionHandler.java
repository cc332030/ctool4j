package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import lombok.CustomLog;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * Description: CHttpRequestMethodNotSupportedExceptionHandler
 * </p>
 *
 * @since 2026/4/9
 */
@CustomLog
@RestControllerAdvice
@Conditional(CHttpRequestMethodNotSupportedExceptionHandler.Condition.class)
public class CHttpRequestMethodNotSupportedExceptionHandler {

    public static class Condition extends CAbstractMissingExceptionHandlerCondition {
        public Condition() {
            super(HttpRequestMethodNotSupportedException.class);
        }
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public CStrResult<Void> handle(HttpRequestMethodNotSupportedException e) {

        log.debug("handle HttpRequestMethodNotSupportedException", e);
        return CStrResult.error(e.getMessage());
    }

}
