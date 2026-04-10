package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import lombok.CustomLog;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * Description: CThrowableHandler
 * </p>
 *
 * @since 2026/4/9
 */
@CustomLog
@RestControllerAdvice
@Conditional(CThrowableHandler.Condition.class)
public class CThrowableHandler {

    static class Condition extends CAbstractMissingExceptionHandlerCondition {
        public Condition() {
            super(Throwable.class);
        }
    }

    @ExceptionHandler(Throwable.class)
    public CStrResult<Void> handle(Throwable e) {

        log.error("handle Throwable", e);
        return CStrResult.error("未知异常");
    }

}
