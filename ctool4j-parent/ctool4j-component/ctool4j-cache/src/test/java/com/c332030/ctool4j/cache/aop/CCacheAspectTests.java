package com.c332030.ctool4j.cache.aop;

import com.c332030.ctool4j.cache.service.CTimeTestService;
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
    CTimeTestService timeService;

    @Test
    @SneakyThrows
    public void cacheAspect() {

        val idA = 1;
        val idB = 2;

        val a1 = timeService.time(idA);
        val b1 = timeService.time(idB);
        TimeUnit.MILLISECONDS.sleep(10);
        val a2 = timeService.time(idA);

        TimeUnit.SECONDS.sleep(1);
        val a3 = timeService.time(idA);

        Assertions.assertEquals(a1, a2);
        Assertions.assertNotEquals(a1, b1);

        Assertions.assertNotEquals(a1, a3);

    }

}
