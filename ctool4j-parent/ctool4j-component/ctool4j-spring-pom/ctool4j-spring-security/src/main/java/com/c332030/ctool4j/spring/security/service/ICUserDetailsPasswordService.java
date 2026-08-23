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
 * @see "doc/design/spring/ICUserDetailsPasswordService.adoc"
 */
public interface ICUserDetailsPasswordService<T> extends UserDetailsPasswordService {

    /**
     * 更新用户密码
     * @param user 用户详情
     * @param newPassword 新密码
     * @return 更新后的用户详情
     */
    @Override
    @SuppressWarnings("unchecked")
    default UserDetails updatePassword(UserDetails user, String newPassword) {
        return updatePassword((CSecurityUser<T>)user, newPassword);
    }

    /**
     * 更新用户密码
     * @param user 用户详情
     * @param newPassword 新密码
     * @return 更新后的用户详情
     */
    CSecurityUser<T> updatePassword(CSecurityUser<T> user, String newPassword);

}
