package com.c332030.ctool4j.spring.security.core;

import com.c332030.ctool4j.spring.security.util.CSpringSecurityUtils;
import lombok.CustomLog;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Description: CAuthenticationEntryPoint
 * </p>
 *
 * @since 2026/1/26
 */
@CustomLog
public class CAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authenticationException
    ) {

        log.debug("CAuthenticationEntryPoint", authenticationException);

        CSpringSecurityUtils.writeJsonError(
            HttpStatus.UNAUTHORIZED,
            "请登录",
            request, response
        );

    }

}
