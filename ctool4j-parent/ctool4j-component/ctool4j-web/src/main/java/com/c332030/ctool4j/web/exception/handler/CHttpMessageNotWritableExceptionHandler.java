package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.exception.annotation.ConditionalOnMissingExceptionHandler;
import lombok.CustomLog;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * Description: CHttpMessageNotWritableExceptionHandler
 * </p>
 *
 * @since 2026/4/9
 */
@CustomLog
@RestControllerAdvice
@ConditionalOnMissingExceptionHandler(HttpMessageNotWritableException.class)
public class CHttpMessageNotWritableExceptionHandler {

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public void handle(HttpMessageNotWritableException e) {
        log.debug("handle HttpMessageNotWritableException，requestURI: {}", CRequestUtils.getRequestURIDefaultNull(), e);
    }

}
