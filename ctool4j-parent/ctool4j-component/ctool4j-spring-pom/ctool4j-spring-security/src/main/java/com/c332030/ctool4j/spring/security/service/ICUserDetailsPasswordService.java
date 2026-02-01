package com.c332030.ctool4j.spring.security.service;

import com.c332030.ctool4j.spring.security.model.CSecurityUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;

/**
 * <p>
 * Description: ICUserDetailsPasswordService
 * </p>
 *
 * @since 2026/2/1
 */
public interface ICUserDetailsPasswordService<T> extends UserDetailsPasswordService {

    @Override
    @SuppressWarnings("unchecked")
    default UserDetails updatePassword(UserDetails user, String newPassword) {
        return updatePassword((CSecurityUser<T>)user, newPassword);
    }

    CSecurityUser<T> updatePassword(CSecurityUser<T> user, String newPassword);

}
