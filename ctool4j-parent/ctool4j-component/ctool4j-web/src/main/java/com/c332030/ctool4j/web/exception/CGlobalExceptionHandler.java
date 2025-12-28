package com.c332030.ctool4j.web.exception;

import com.c332030.ctool4j.definition.model.result.impl.CIntResult;
import lombok.CustomLog;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.context.annotation.Primary;
import org.springframework.core.PriorityOrdered;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * Description: CGlobalExceptionHandler
 * </p>
 *
 * @since 2025/10/27
 */
@CustomLog
@Primary
@RestControllerAdvice
public class CGlobalExceptionHandler implements PriorityOrdered {

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    @ExceptionHandler({
            HttpRequestMethodNotSupportedException.class,
            ClientAbortException.class,
            HttpMessageNotWritableException.class,
    })
    public CIntResult<Object> handleIgnoreException(Throwable e) {

        log.debug("handleIgnoreException", e);
        return CIntResult.error(e.getMessage());
    }

}
