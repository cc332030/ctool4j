package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import lombok.CustomLog;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * Description: CExceptionHandler
 * </p>
 *
 * @since 2026/4/9
 */
@CustomLog
@RestControllerAdvice
@Conditional(CExceptionHandlerCondition.class)
public class CExceptionHandler {

    @ExceptionHandler({
        Exception.class,
    })
    public CStrResult<Void> cHandleException(Exception e) {

        log.debug("handle Exception", e);
        return CStrResult.error("未知异常");
    }

}
