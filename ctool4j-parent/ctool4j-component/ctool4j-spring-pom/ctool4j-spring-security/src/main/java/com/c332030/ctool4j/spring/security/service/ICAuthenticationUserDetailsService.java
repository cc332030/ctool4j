package com.c332030.ctool4j.spring.security.service;

import com.c332030.ctool4j.spring.security.model.CSecurityUser;
import lombok.val;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * <p>
 * Description: ICAuthenticationUserDetailsService
 * </p>
 *
 * @since 2026/2/1
 * @see "doc/design/spring/ICAuthenticationUserDetailsService.adoc"
 */
public interface ICAuthenticationUserDetailsService<U, T extends Authentication>
    extends AuthenticationUserDetailsService<T> {

    /**
     * 根据令牌加载用户（用户不存在时抛出异常）
     * @param token 认证令牌
     * @return 用户详情
     * @throws UsernameNotFoundException 用户不存在
     */
    @Override
    default UserDetails loadUserDetails(T token) throws UsernameNotFoundException {

        val user = loadTokenDetail(token);
        if(user == null){
            throw new IllegalStateException("Can't find user by token: " + token);
        }
        return user;
    }

    /**
     * 根据令牌加载用户
     * @param token 认证令牌
     * @return 用户详情
     * @throws UsernameNotFoundException 用户不存在
     */
    CSecurityUser<U> loadTokenDetail(T token) throws UsernameNotFoundException;

}
