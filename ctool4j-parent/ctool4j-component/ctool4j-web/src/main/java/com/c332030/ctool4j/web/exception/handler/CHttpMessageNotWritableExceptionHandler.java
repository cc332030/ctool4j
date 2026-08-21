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
 * @see "doc/design/web/CHttpMessageNotWritableExceptionHandler.adoc"
 */
@CustomLog
@RestControllerAdvice
@ConditionalOnMissingExceptionHandler(HttpMessageNotWritableException.class)
public class CHttpMessageNotWritableExceptionHandler {

    /**
     * 处理响应消息不可写异常，仅记录日志
     *
     * @param e 响应消息不可写异常
     */
    @ExceptionHandler(HttpMessageNotWritableException.class)
    public void handle(HttpMessageNotWritableException e) {
        log.debug("handle HttpMessageNotWritableException，requestURI: {}", CRequestUtils.getRequestURIDefaultNull(), e);
    }

}
