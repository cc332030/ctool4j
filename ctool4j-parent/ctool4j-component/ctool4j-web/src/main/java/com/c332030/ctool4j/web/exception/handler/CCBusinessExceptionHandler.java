package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.core.exception.CBusinessException;
import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import lombok.CustomLog;
import org.springframework.context.annotation.Conditional;
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
@Conditional(CCBusinessExceptionHandler.Condition.class)
public class CCBusinessExceptionHandler {

    public static class Condition extends CAbstractMissingExceptionHandlerCondition {
        public Condition() {
            super(CBusinessException.class);
        }
    }

    @ExceptionHandler(CBusinessException.class)
    public CStrResult<Void> handle(CBusinessException e) {

        log.debug("handle CBusinessException", e);
        return CStrResult.error("未知异常");
    }

}
