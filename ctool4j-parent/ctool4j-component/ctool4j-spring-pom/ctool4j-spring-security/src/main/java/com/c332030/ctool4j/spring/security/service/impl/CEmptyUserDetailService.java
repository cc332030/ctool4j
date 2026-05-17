package com.c332030.ctool4j.spring.security.service.impl;

import com.c332030.ctool4j.spring.security.model.CSecurityUser;
import com.c332030.ctool4j.spring.security.service.ICUserDetailsService;

/**
 * <p>
 * Description: CEmptyUserDetailService
 * </p>
 *
 * @since 2026/4/9
 */
public class CEmptyUserDetailService implements ICUserDetailsService<Void> {

    @Override
    public CSecurityUser<Void> loadByUsername(String username) {
        throw new UnsupportedOperationException("未避免报错的默认空实现，username: " + username);
    }

}
