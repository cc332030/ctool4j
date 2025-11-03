package com.c332030.ctool4j.web.advice;

import lombok.CustomLog;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * <p>
 * Description: CGlobalExceptionHandler
 * </p>
 *
 * @since 2025/10/27
 */
@CustomLog
//@RestControllerAdvice
public class CGlobalExceptionHandler {

    @ExceptionHandler({
            HttpRequestMethodNotSupportedException.class,
            ClientAbortException.class,
            HttpMessageNotWritableException.class,
    })
    public void handle(Throwable e) {
        log.debug("", e);
    }

}
