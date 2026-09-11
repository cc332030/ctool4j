package com.c332030.ctool4j.spring.security.service;

import com.c332030.ctool4j.spring.security.model.CSecurityUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;

/**
 * <p>
 * Description: ICUserDetailsPasswordService
 * </p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>更新用户密码</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>密码更新</p>
 * <h2>不适用与边界场景</h2>
 * <p>接口定义</p>
 * <h2>已知限制与取舍</h2>
 * <p>接口定义</p>
 *
 * @since 2026/2/1
 * @version 1.0
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
