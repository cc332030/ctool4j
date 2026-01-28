package com.c332030.ctool4j.spring.security.advice;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import lombok.CustomLog;
import org.springframework.context.annotation.Primary;
import org.springframework.core.PriorityOrdered;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 * Description: CGlobalExceptionHandler
 * </p>
 *
 * @since 2025/10/27
 */
@CustomLog
@Primary
//@ControllerAdvice
public class CSpringSecurityExceptionHandler implements PriorityOrdered {

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    /**
     * 兜底
     */
    @ResponseBody
    @ExceptionHandler(AuthenticationException.class)
    public CStrResult<Object> cHandleAuthenticationException(AuthenticationException e) {

        log.debug("handle AuthenticationException", e);
        return CStrResult.error(e.getMessage());
    }

    /**
     * 未登录/登录过期
     */
    @ResponseBody
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public CStrResult<Object> cHandleAuthenticationCredentialsNotFoundException(AuthenticationCredentialsNotFoundException e) {

        log.debug("handle AuthenticationCredentialsNotFoundException", e);
        return CStrResult.error(e.getMessage());
    }

    /**
     * 无权限
     */
    @ResponseBody
    @ExceptionHandler(AccessDeniedException.class)
    public CStrResult<Object> cHandleAccessDeniedException(AccessDeniedException e) {

        log.debug("handle AccessDeniedException", e);
        return CStrResult.error(e.getMessage());
    }

}
