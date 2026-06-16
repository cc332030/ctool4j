package com.c332030.ctool4j.cache.aop;

import com.c332030.ctool4j.cache.model.CCacheUser;
import com.c332030.ctool4j.cache.service.CCacheTestService;
import com.c332030.ctool4j.spring.test.annotation.CTool4jSpringBootTest;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Description: CCacheAspectTests
 * </p>
 *
 * @since 2026/6/16
 */
@CTool4jSpringBootTest
public class CCacheAspectTests {

    @Autowired
    CCacheTestService cacheTestService;

    @Test
    @SneakyThrows
    public void cacheAspect() {

        val idA = 1;
        val idB = 2;

        val a1 = cacheTestService.time(idA);
        val b1 = cacheTestService.time(idB);

        val cacheUser = CCacheUser.builder()
            .id(System.currentTimeMillis())
            .build();
        val uA1 = cacheTestService.userCache(cacheUser);

        TimeUnit.MILLISECONDS.sleep(10);
        val a2 = cacheTestService.time(idA);
        val uA2 = cacheTestService.userCache(cacheUser);

        TimeUnit.SECONDS.sleep(1);
        val a3 = cacheTestService.time(idA);
        val uA3 = cacheTestService.userCache(cacheUser);

        Assertions.assertEquals(a1, a2);
        Assertions.assertEquals(uA1, uA2);

        Assertions.assertNotEquals(a1, b1);

        Assertions.assertNotEquals(a1, a3);
        Assertions.assertNotEquals(uA1, uA3);

    }

}
