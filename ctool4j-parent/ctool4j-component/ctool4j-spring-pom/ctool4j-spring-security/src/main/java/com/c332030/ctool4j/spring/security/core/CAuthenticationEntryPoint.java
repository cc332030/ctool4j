package com.c332030.ctool4j.spring.security.core;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.web.util.CServletUtils;
import lombok.val;
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
public class CAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) {

        val requestUrl = request.getRequestURI();

        val forbidden = HttpStatus.FORBIDDEN;
        val forbiddenResult = CStrResult.error(
            String.valueOf(forbidden.value()),
            "未授权：" + requestUrl
        );

        CServletUtils.writeJson(response, forbidden, forbiddenResult);

    }

}
