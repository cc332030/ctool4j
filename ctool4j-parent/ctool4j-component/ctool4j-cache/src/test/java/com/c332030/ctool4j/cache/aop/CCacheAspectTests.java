package com.c332030.ctool4j.cache.aop;

import com.c332030.ctool4j.cache.annotation.CCacheable;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * <p>
 * Description: CCacheAspectTests
 * </p>
 *
 * @since 2026/6/16
 */
@SpringBootTest
public class CCacheAspectTests {

    @CCacheable(
        namespace = CCacheAspectTests.class
    )
    public Long time(Integer id) {
        return System.currentTimeMillis();
    }

}
