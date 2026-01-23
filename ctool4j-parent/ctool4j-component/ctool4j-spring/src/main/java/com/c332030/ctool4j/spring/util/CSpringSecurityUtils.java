package com.c332030.ctool4j.spring.util;

import com.c332030.ctool4j.core.classes.CObjUtils;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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

}
