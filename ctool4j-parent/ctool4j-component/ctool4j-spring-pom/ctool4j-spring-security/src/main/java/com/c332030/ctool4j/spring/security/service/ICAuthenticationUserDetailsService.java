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
 * <h2>设计要点</h2>
 * <ul>
 *   <li>按令牌加载用户，不存在抛异常</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>抛异常</p>
 * <h2>适用范围</h2>
 * <p>认证用户加载</p>
 * <h2>不适用与边界场景</h2>
 * <p>接口定义</p>
 * <h2>已知限制与取舍</h2>
 * <p>接口定义</p>
 *
 * @since 2026/2/1
 * @version 1.0
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
