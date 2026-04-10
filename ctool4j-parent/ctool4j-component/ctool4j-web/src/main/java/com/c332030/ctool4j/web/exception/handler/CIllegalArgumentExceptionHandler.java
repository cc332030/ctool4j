package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import lombok.CustomLog;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * Description: CIllegalArgumentExceptionHandler
 * </p>
 *
 * @since 2026/4/9
 */
@CustomLog
@RestControllerAdvice
@Conditional(CIllegalArgumentExceptionHandler.Condition.class)
public class CIllegalArgumentExceptionHandler {

    static class Condition extends CAbstractMissingExceptionHandlerCondition {
        public Condition() {
            super(IllegalArgumentException.class);
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public CStrResult<Void> handle(IllegalArgumentException e) {

        log.debug("handle IllegalArgumentException", e);
        return CStrResult.error(e.getMessage());
    }

}
