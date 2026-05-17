package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.exception.annotation.ConditionalOnMissingExceptionHandler;
import lombok.CustomLog;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * Description: CHttpRequestMethodNotSupportedExceptionHandler
 * </p>
 *
 * @since 2026/4/9
 */
@CustomLog
@RestControllerAdvice
@ConditionalOnMissingExceptionHandler(HttpRequestMethodNotSupportedException.class)
public class CHttpRequestMethodNotSupportedExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public CStrResult<Void> handle(HttpRequestMethodNotSupportedException e) {

        log.debug("handle HttpRequestMethodNotSupportedException，requestURI: {}", CRequestUtils.getRequestURIDefaultNull(), e);
        return CStrResult.error(e.getMessage());
    }

}
