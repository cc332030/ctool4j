package com.c332030.ctool4j.cache.service;

import com.c332030.ctool4j.cache.annotation.CCacheable;
import com.c332030.ctool4j.cache.aop.CCacheAspectTests;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Description: CTimeTestService
 * </p>
 *
 * @since 2026/6/16
 */
@Service
public class CTimeTestService {

    @CCacheable(
        namespace = CCacheAspectTests.class,
        expire = 1
    )
    public Long time(Integer id) {
        return System.currentTimeMillis();
    }

}
