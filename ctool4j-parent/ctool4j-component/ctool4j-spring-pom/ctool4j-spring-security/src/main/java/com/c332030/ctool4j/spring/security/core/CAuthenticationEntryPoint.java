package com.c332030.ctool4j.spring.security.core;

import com.c332030.ctool4j.spring.security.util.CSpringSecurityUtils;
import lombok.CustomLog;
import lombok.var;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
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
 * @see "doc/design/spring/CAuthenticationEntryPoint.adoc"
 * @see "doc/design/spring/CAuthenticationEntryPointTests.adoc"
 */
@CustomLog
public class CAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * 处理未认证访问：根据异常类型生成提示消息并输出 401 错误响应
     *
     * @param request               请求
     * @param response              响应
     * @param authenticationException 认证异常
     */
    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authenticationException
    ) {

        log.debug("CAuthenticationEntryPoint", authenticationException);

        var message = "";
        if(authenticationException instanceof AuthenticationCredentialsNotFoundException) {
            message = "无有效登录用户";
        } else if(authenticationException instanceof BadCredentialsException) {
            message = authenticationException.getMessage();
        }

        CSpringSecurityUtils.writeJsonError(
            HttpStatus.UNAUTHORIZED,
            message,
            request, response
        );

    }

}
