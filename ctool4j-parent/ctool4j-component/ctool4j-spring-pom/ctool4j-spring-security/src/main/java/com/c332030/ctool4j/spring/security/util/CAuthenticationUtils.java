package com.c332030.ctool4j.spring.security.util;

import com.c332030.ctool4j.definition.interfaces.ICUsernameAndPassword;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import com.c332030.ctool4j.spring.security.model.CSecurityUser;
import lombok.Setter;
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
@CAutowiredScan
public class CAuthenticationUtils {

    @Setter
    @CAutowired
    PasswordEncoder passwordEncoder;

    @Setter
    @CAutowired
    AuthenticationManager authenticationManager;

    /**
     * 密码编码
     *
     * @param password 原始密码
     * @return 编码后的密码
     */
    public String encode(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * 密码校验
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 编码后的密码
     * @return true 表示匹配
     */
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 认证
     *
     * @param authentication 认证信息
     * @return 认证后的认证信息
     */
    public Authentication authenticate(Authentication authentication) {
        return authenticationManager.authenticate(authentication);
    }

    /**
     * 按用户名密码认证并写入安全上下文
     *
     * @param req 用户名密码请求
     */
    public void authenticate(ICUsernameAndPassword req) {

        val authenticationToken = UsernamePasswordAuthenticationToken.unauthenticated(
            req.getUsername(),
            req.getPassword()
        );

        val authentication = authenticate(authenticationToken);
        CSpringSecurityUtils.setAuthentication(authentication);

    }

    /**
     * 获取当前安全用户
     *
     * @param <T> 业务用户类型
     * @return 当前安全用户
     */
    public <T> CSecurityUser<T> getSecurityUser() {
        return CSpringSecurityUtils.getUserDetails();
    }

    /**
     * 获取当前业务用户
     *
     * @param <T> 业务用户类型
     * @return 当前业务用户
     */
    @SuppressWarnings("unchecked")
    public <T> T getUser() {
        return (T)getSecurityUser().getUser();
    }

}
