package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import lombok.CustomLog;
import lombok.val;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * <p>
 * Description: CMethodArgumentNotValidExceptionHandler
 * </p>
 *
 * @since 2026/4/9
 */
@CustomLog
@RestControllerAdvice
@Conditional(CMethodArgumentNotValidExceptionHandler.Condition.class)
public class CMethodArgumentNotValidExceptionHandler {

    public static class Condition extends CAbstractMissingExceptionHandlerCondition {
        public Condition() {
            super(MethodArgumentNotValidException.class);
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CStrResult<Void> handle(MethodArgumentNotValidException e) {

        log.debug("参数校验错误，requestURI: {}", CRequestUtils.getRequestURIDefaultNull(), e);

        val message = e.getBindingResult().getFieldErrors()
            .stream().map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
            .collect(Collectors.joining("，"));
        return CStrResult.error(message);
    }

}
