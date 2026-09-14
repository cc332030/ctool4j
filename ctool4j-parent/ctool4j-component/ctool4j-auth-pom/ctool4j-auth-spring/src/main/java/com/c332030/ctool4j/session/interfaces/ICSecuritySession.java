package com.c332030.ctool4j.session.interfaces;

import com.c332030.ctool4j.spring.security.util.CSpringSecurityUtils;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
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
 * @version 1.1
 */
public interface ICSecuritySession extends ICSession {

    /**
     * 会话关联的权限
     *
     * <p>默认返回匿名权限的<b>副本</b>，而不是直接返回共享常量
     * {@link CSpringSecurityUtils#ANONYMOUS_AUTHORITIES}：Jackson 的
     * {@code MapperFeature.USE_GETTERS_AS_SETTERS}（默认开启）在本属性无可写成员（无 setter、无可见字段）时走
     * {@code SetterlessProperty}，会把 JSON 中的 {@code authorities} <b>合并进本方法的返回值</b>——
     * 返回共享的不可修改常量会抛 {@code UnsupportedOperationException}（线上 loadAuthentication error 的根因），
     * 返回共享的可变常量则会被逐次追加而污染全局。返回一次性副本让该合并既不报错也不留下副作用（语义上等同忽略）。</p>
     *
     * <p>会话若自身持久化权限，请提供可写成员（setter 或可见字段）：Jackson 会改走 setter 路径，不受本默认实现影响。</p>
     *
     * @return 权限集合（匿名权限的副本），默认匿名权限
     */
    default Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<>(CSpringSecurityUtils.ANONYMOUS_AUTHORITIES);
    }

}
