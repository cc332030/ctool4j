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
 * @since 2026/1/24
 */
@Getter
public class CSecurityUser<T> extends User {

    private static final long serialVersionUID = 1L;

    final T user;

    public CSecurityUser(
        ICUsernameAndPassword usernameAndPassword,
        T user,
        Collection<? extends GrantedAuthority> authorities
    ) {
        super(usernameAndPassword.getUsername(), usernameAndPassword.getPassword(), authorities);
        this.user = user;
    }

}
