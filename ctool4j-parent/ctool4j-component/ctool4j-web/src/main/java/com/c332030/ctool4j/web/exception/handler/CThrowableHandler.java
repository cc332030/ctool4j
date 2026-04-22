package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.exception.annotation.ConditionalOnMissingExceptionHandler;
import lombok.CustomLog;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * Description: CThrowableHandler
 * </p>
 *
 * @since 2026/4/9
 */
@CustomLog
@RestControllerAdvice
@ConditionalOnMissingExceptionHandler(Throwable.class)
public class CThrowableHandler {

    @ExceptionHandler(Throwable.class)
    public CStrResult<Void> handle(Throwable e) {

        log.error("handle Throwable，requestURI: {}", CRequestUtils.getRequestURIDefaultNull(), e);
        return CStrResult.error("未知异常");
    }

}
