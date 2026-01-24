package com.c332030.ctool4j.spring.security.util;

import cn.hutool.core.collection.CollUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.core.util.CArrUtils;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Collection;

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

    public RequestMatcher[] toPathRequestMatchers(Collection<String> patterns) {

        if(CollUtil.isEmpty(patterns)) {
            return CArrUtils.emptyArray();
        }

        return patterns.stream()
            .map(AntPathRequestMatcher::new)
            .toArray(RequestMatcher[]::new);
    }

}
