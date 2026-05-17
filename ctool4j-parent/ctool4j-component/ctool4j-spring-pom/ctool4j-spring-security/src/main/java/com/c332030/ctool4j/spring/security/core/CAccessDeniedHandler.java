package com.c332030.ctool4j.spring.security.core;

import com.c332030.ctool4j.spring.security.util.CSpringSecurityUtils;
import lombok.CustomLog;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Description: CAccessDeniedHandler
 * </p>
 *
 * @since 2026/1/28
 */
@CustomLog
public class CAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException
    ) {

        log.debug("CAccessDeniedHandler", accessDeniedException);

        CSpringSecurityUtils.writeJsonError(
            HttpStatus.FORBIDDEN,
            request, response
        );

    }

}
