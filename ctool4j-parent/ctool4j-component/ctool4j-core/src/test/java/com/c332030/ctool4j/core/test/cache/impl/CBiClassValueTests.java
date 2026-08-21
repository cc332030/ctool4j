package com.c332030.ctool4j.core.test.cache.impl;

import com.c332030.ctool4j.core.cache.impl.CBiClassValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Description: CBiClassValueTests
 * </p>
 *
 * @since 2025/12/12
 */
public class CBiClassValueTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void get() {

        CBiClassValue<String> cv = CBiClassValue.of((t1, t2) -> t1.getSimpleName() + "-" + t2.getSimpleName());
        Assertions.assertEquals("String-Integer", cv.get(String.class, Integer.class));

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void cached() {

        AtomicInteger counter = new AtomicInteger();
        CBiClassValue<String> cv = CBiClassValue.of((t1, t2) -> {
            counter.incrementAndGet();
            return t1.getSimpleName() + "-" + t2.getSimpleName();
        });

        cv.get(String.class, Integer.class);
        cv.get(String.class, Integer.class);
        Assertions.assertEquals(1, counter.get());

    }

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void differentPairComputeSeparately() {

        AtomicInteger counter = new AtomicInteger();
        CBiClassValue<String> cv = CBiClassValue.of((t1, t2) -> {
            counter.incrementAndGet();
            return t1.getSimpleName() + "-" + t2.getSimpleName();
        });

        cv.get(String.class, Integer.class);
        cv.get(String.class, Long.class);
        Assertions.assertEquals(2, counter.get());

    }

}
