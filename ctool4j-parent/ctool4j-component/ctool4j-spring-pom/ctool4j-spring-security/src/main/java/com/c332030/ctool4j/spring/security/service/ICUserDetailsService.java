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
 */
public interface ICUserDetailsService<T> extends UserDetailsService {

    @Override
    default UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        val user = loadByUsername(username);
        if(user==null){
            throw new UsernameNotFoundException(username);
        }

        return user;
    }

    CSecurityUser<T> loadByUsername(String username);

}
