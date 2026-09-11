package com.c332030.ctool4j.session.interfaces;

import com.c332030.ctool4j.spring.security.util.CSpringSecurityUtils;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * <p>
 * Description: ICSecuritySession
 * </p>
 *
 * <p>Spring Security 会话接口，继承 {@link ICSession}，提供权限获取能力，
 * 用于需要构造 Spring Security 认证信息（如 {@code UsernamePasswordAuthenticationToken}）的场景。</p>
 *
 * <p>拆分说明：{@link ICSession}（auth-base）保持不依赖 Spring Security；与 Spring Security 强相关的
 * 权限能力下沉到本接口（auth-spring），便于后期不引入 spring-security 的场景仅用 {@link ICSession}。</p>
 *
 * @author c332030
 * @since 2026/9/11
 */
public interface ICSecuritySession extends ICSession {

    /**
     * 会话关联的权限
     *
     * @return 权限集合，默认匿名权限
     */
    default Collection<? extends GrantedAuthority> getAuthorities() {
        return CSpringSecurityUtils.ANONYMOUS_AUTHORITIES;
    }

}
