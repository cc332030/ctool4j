package com.c332030.ctool4j.web.advice;

import com.c332030.ctool4j.definition.model.CResult;
import lombok.CustomLog;
import org.apache.catalina.connector.ClientAbortException;
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
@RestControllerAdvice
public class CGlobalExceptionHandler {

    @ExceptionHandler({
            HttpRequestMethodNotSupportedException.class,
            ClientAbortException.class,
            HttpMessageNotWritableException.class,
    })
    public CResult<Object> handleIgnoreException(Throwable e) {

        log.info("handleIgnoreException", e);
        return CResult.error(e.getMessage());
    }

}
