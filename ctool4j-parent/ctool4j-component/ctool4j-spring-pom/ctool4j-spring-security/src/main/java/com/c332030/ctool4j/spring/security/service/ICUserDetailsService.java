package com.c332030.ctool4j.spring.security.service;

import com.c332030.ctool4j.spring.security.model.CSecurityUser;
import lombok.val;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * <p>
 * Description: ICUserDetailsService
 * </p>
 *
 * @since 2026/2/1
 * @see doc/design/spring/ICUserDetailsService.adoc
 */
public interface ICUserDetailsService<T> extends UserDetailsService {

    /**
     * 根据用户名加载用户（用户不存在时抛出异常）
     * @param username 用户名
     * @return 用户详情
     * @throws UsernameNotFoundException 用户不存在
     */
    @Override
    default UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        val user = loadByUsername(username);
        if(user==null){
            throw new UsernameNotFoundException(username);
        }

        return user;
    }

    /**
     * 根据用户名加载用户
     * @param username 用户名
     * @return 用户详情
     */
    CSecurityUser<T> loadByUsername(String username);

}
