package com.c332030.ctool4j.spring.security.service.impl;

import com.c332030.ctool4j.spring.security.model.CSecurityUser;
import com.c332030.ctool4j.spring.security.service.ICUserDetailsService;

/**
 * <p>
 * Description: CEmptyUserDetailService
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CEmptyUserDetailService}：空用户服务。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>默认空实现，loadUserByUsername 抛 UnsupportedOperationException</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>抛异常</p>
 * <h2>适用范围</h2>
 * <p>空实现兜底</p>
 * <h2>不适用与边界场景</h2>
 * <p>实现 ICUserDetailsService</p>
 * <h2>已知限制与取舍</h2>
 * <p>实现 ICUserDetailsService</p>
 *
 * @since 2026/4/9
 * @version 1.0
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
