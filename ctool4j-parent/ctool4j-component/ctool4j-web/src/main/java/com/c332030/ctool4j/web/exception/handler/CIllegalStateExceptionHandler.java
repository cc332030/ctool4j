package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import lombok.CustomLog;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * Description: CIllegalStateExceptionHandler
 * </p>
 *
 * @since 2026/4/9
 */
@CustomLog
@RestControllerAdvice
@Conditional(CIllegalStateExceptionHandler.Condition.class)
public class CIllegalStateExceptionHandler {

    static class Condition extends CAbstractMissingExceptionHandlerCondition {
        public Condition() {
            super(IllegalStateException.class);
        }
    }

    @ExceptionHandler(IllegalStateException.class)
    public CStrResult<Void> handle(IllegalStateException e) {

        log.debug("handle IllegalStateException，requestURI: {}", CRequestUtils.getRequestURIDefaultNull(), e);
        return CStrResult.error(e.getMessage());
    }

}
