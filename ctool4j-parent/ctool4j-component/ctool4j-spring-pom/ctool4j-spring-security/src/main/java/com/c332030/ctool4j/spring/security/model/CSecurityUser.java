package com.c332030.ctool4j.spring.security.model;

import com.c332030.ctool4j.core.util.CList;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * <p>
 * Description: CSecurityUser
 * </p>
 *
 * @since 2026/1/24
 */
@Getter
public class CSecurityUser<T> extends User {

    private static final long serialVersionUID = 1L;

    final T user;

    public CSecurityUser(
        String username, String password,
        T user
    ) {
        this(username, password, user, CList.of());
    }

    public CSecurityUser(
        String username, String password,
        T user,
        Collection<? extends GrantedAuthority> authorities
    ) {
        super(username, password, authorities);
        this.user = user;
    }

}
