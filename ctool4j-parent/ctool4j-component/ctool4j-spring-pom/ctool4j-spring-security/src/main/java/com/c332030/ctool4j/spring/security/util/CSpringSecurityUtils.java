package com.c332030.ctool4j.spring.security.util;

import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.web.util.CServletUtils;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Description: CSpringSecurityUtils
 * </p>
 *
 * @since 2026/1/23
 */
@UtilityClass
public class CSpringSecurityUtils {

    public SecurityContext getContext() {
        return SecurityContextHolder.getContext();
    }

    public Authentication getAuthentication() {
        return getContext().getAuthentication();
    }

    public void setAuthentication(Authentication authentication) {
        getContext().setAuthentication(authentication);
    }

    public <T extends UserDetails> T getUserDetails() {

        val authentication = getAuthentication();
        if(authentication == null
            || !authentication.isAuthenticated()
        ) {
            return null;
        }

        return CObjUtils.anyType(authentication.getPrincipal());
    }

    public void writeJsonError(
        HttpStatus httpStatus,
        String message,
        HttpServletRequest request,
        HttpServletResponse response
    ) {

        val requestUrl = request.getRequestURI();

        val forbiddenResult = CStrResult.error(
            String.valueOf(httpStatus.value()),
            message + "：" + requestUrl
        );

        CServletUtils.writeJson(response, httpStatus, forbiddenResult);
    }

}
