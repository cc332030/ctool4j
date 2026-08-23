package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.exception.annotation.ConditionalOnMissingExceptionHandler;
import lombok.CustomLog;
import lombok.val;
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
 * @see "doc/design/web/CMethodArgumentNotValidExceptionHandler.adoc"
 */
@CustomLog
@RestControllerAdvice
@ConditionalOnMissingExceptionHandler(MethodArgumentNotValidException.class)
public class CMethodArgumentNotValidExceptionHandler {

    /**
     * 处理参数校验异常，拼接全部字段错误信息
     *
     * <p>每个字段错误展示为"字段名 + 校验消息"（如 "name 不能为空"），多个字段错误以"，"分隔。</p>
     *
     * @param e 参数校验异常
     * @return 错误结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CStrResult<Void> handle(MethodArgumentNotValidException e) {

        log.debug("参数校验错误，requestURI: {}", CRequestUtils.getRequestURIDefaultNull(), e);

        val message = e.getBindingResult().getFieldErrors()
            .stream().map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
            .collect(Collectors.joining("，"));
        return CStrResult.error(message);
    }

}
