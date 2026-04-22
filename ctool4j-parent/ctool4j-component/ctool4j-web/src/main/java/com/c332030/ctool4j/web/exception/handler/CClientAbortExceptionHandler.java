package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.spring.util.CRequestUtils;
import lombok.CustomLog;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * Description: CClientAbortExceptionHandler
 * </p>
 *
 * @since 2026/4/9
 */
@CustomLog
@RestControllerAdvice
@Conditional(CClientAbortExceptionHandler.Condition.class)
public class CClientAbortExceptionHandler {

    static class Condition extends CAbstractMissingExceptionHandlerCondition {
        public Condition() {
            super(ClientAbortException.class);
        }
    }

    @ExceptionHandler(ClientAbortException.class)
    public void handle(ClientAbortException e) {
        log.debug("handle ClientAbortException，requestURI: {}", CRequestUtils.getRequestURIDefaultNull(), e);
    }

}
