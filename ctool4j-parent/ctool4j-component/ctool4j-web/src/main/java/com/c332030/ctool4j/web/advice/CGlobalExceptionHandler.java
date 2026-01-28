package com.c332030.ctool4j.web.advice;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import lombok.CustomLog;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.context.annotation.Primary;
import org.springframework.core.PriorityOrdered;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 * Description: CGlobalExceptionHandler
 * </p>
 *
 * @since 2025/10/27
 */
@CustomLog
@Primary
@ControllerAdvice
public class CGlobalExceptionHandler implements PriorityOrdered {

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    @ResponseBody
    @ExceptionHandler({
        HttpRequestMethodNotSupportedException.class,
    })
    public CStrResult<Object> cHandleReturnableIgnoreException(Throwable e) {

        log.debug("handle ReturnableIgnoreException", e);
        return CStrResult.error(e.getMessage());
    }

    @ExceptionHandler({
        HttpMessageNotWritableException.class,
        ClientAbortException.class,
    })
    public void cHandleNonReturnableIgnoreException(Throwable e) {
        log.debug("handle NonReturnableIgnoreException", e);
    }

}
