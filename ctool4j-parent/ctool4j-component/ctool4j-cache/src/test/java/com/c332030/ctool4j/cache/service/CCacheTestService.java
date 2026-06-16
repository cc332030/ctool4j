package com.c332030.ctool4j.cache.service;

import com.c332030.ctool4j.cache.annotation.CCacheable;
import com.c332030.ctool4j.cache.aop.CCacheAspectTests;
import com.c332030.ctool4j.cache.model.CCacheUser;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Description: CCacheTestService
 * </p>
 *
 * @since 2026/6/16
 */
@Service
public class CCacheTestService {

    @CCacheable(
        namespace = CCacheAspectTests.class,
        expire = 1
    )
    public Long time(Integer id) {
        return System.currentTimeMillis();
    }

    @CCacheable(
        namespace = CCacheAspectTests.class,
        expire = 1
    )
    public Long userCache(CCacheUser cacheUser) {
        return System.currentTimeMillis();
    }

}
