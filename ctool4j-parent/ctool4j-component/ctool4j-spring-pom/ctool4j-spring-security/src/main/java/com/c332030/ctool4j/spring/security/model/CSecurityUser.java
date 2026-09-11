package com.c332030.ctool4j.spring.security.model;

import com.c332030.ctool4j.definition.interfaces.ICUsernameAndPassword;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * <p>
 * Description: CSecurityUser
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CSecurityUser}：安全用户模型。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>业务用户信息载体（含用户名、权限等）</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>安全用户载体</p>
 * <h2>不适用与边界场景</h2>
 * <p>实现 UserDetails</p>
 * <h2>已知限制与取舍</h2>
 * <p>实现 UserDetails</p>
 *
 * @since 2026/1/24
 * @version 1.0
 */
@Getter
public class CSecurityUser<T> extends User {

    private static final long serialVersionUID = 1L;

    /**
     * 业务用户
     */
    final T user;

    /**
     * 构造方法
     *
     * @param usernameAndPassword 用户名密码信息
     * @param user                业务用户
     * @param authorities         权限集合
     */
    public CSecurityUser(
        ICUsernameAndPassword usernameAndPassword,
        T user,
        Collection<? extends GrantedAuthority> authorities
    ) {
        super(usernameAndPassword.getUsername(), usernameAndPassword.getPassword(), authorities);
        this.user = user;
    }

}
