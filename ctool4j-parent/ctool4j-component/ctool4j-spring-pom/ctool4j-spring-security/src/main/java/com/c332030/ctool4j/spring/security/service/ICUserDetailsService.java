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
 * <h2>能力目录</h2>
 * <p>{@code ICUserDetailsService}：用户名加载用户服务。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>按用户名加载用户，不存在抛异常</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>抛异常</p>
 * <h2>适用范围</h2>
 * <p>用户加载</p>
 * <h2>不适用与边界场景</h2>
 * <p>接口定义</p>
 * <h2>已知限制与取舍</h2>
 * <p>接口定义</p>
 *
 * @since 2026/2/1
 * @version 1.0
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
