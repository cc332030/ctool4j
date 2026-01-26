package com.c332030.ctool4j.spring.security.util;

import com.c332030.ctool4j.definition.interfaces.ICUsernameAndPassword;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.security.model.CSecurityUser;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * <p>
 * Description: CAuthenticationUtils
 * </p>
 *
 * @since 2026/1/24
 */
@UtilityClass
public class CAuthenticationUtils {

    @CAutowired
    PasswordEncoder passwordEncoder;

    @CAutowired
    AuthenticationManager authenticationManager;

    public String encode(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public Authentication authenticate(Authentication authentication) {
        return authenticationManager.authenticate(authentication);
    }

    public void authenticate(ICUsernameAndPassword req) {

        val authenticationToken = UsernamePasswordAuthenticationToken.unauthenticated(
            req.getUsername(),
            req.getPassword()
        );

        val authentication = authenticate(authenticationToken);
        CSpringSecurityUtils.setAuthentication(authentication);

    }

    public <T extends ICUsernameAndPassword> CSecurityUser<T> getSecurityUser() {
        return CSpringSecurityUtils.getUserDetails();
    }

    @SuppressWarnings("unchecked")
    public <T extends ICUsernameAndPassword> T getUser() {
        return (T)getSecurityUser().getUser();
    }

}
