package com.c332030.ctool4j.spring.security.service.impl;

import com.c332030.ctool4j.spring.security.model.CSecurityUser;
import com.c332030.ctool4j.spring.security.service.ICUserDetailsService;

/**
 * <p>
 * Description: CEmptyUserDetailService
 * </p>
 *
 * @since 2026/4/9
 * @see doc/design/spring/CEmptyUserDetailService.adoc
 */
public class CEmptyUserDetailService implements ICUserDetailsService<Void> {

    /**
     * 加载用户：默认空实现，直接抛 UnsupportedOperationException
     *
     * @param username 用户名
     * @return 安全用户
     */
    @Override
    public CSecurityUser<Void> loadByUsername(String username) {
        throw new UnsupportedOperationException("未避免报错的默认空实现，username: " + username);
    }

}
