package com.c332030.ctool4j.spring.security.model;

import com.c332030.ctool4j.core.util.CList;
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
public class CSecurityUser<T extends ICUsernameAndPassword> extends User {

    private static final long serialVersionUID = 1L;

    final T user;

    public CSecurityUser(T user) {
        this(user, CList.of());
    }

    public CSecurityUser(T user, Collection<? extends GrantedAuthority> authorities) {
        super(user.getUsername(), user.getPassword(), authorities);
        this.user = user;
    }

}
