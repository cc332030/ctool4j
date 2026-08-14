package com.c332030.ctool4j.core.test.cache.impl;

import com.c332030.ctool4j.core.cache.impl.CClassValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Description: CClassValueTests
 * </p>
 *
 * @since 2025/12/12
 */
public class CClassValueTests {

    @Test
    public void get() {

        CClassValue<String> cv = CClassValue.of(clazz -> clazz.getSimpleName());
        Assertions.assertEquals("String", cv.get(String.class));
        Assertions.assertEquals("Integer", cv.get(Integer.class));

    }

    @Test
    public void cached() {

        AtomicInteger counter = new AtomicInteger();
        CClassValue<String> cv = CClassValue.of(clazz -> {
            counter.incrementAndGet();
            return clazz.getSimpleName();
        });

        cv.get(String.class);
        cv.get(String.class);
        Assertions.assertEquals(1, counter.get());

    }

    @Test
    public void differentClassComputeSeparately() {

        AtomicInteger counter = new AtomicInteger();
        CClassValue<String> cv = CClassValue.of(clazz -> {
            counter.incrementAndGet();
            return clazz.getSimpleName();
        });

        cv.get(String.class);
        cv.get(Integer.class);
        cv.get(String.class);
        Assertions.assertEquals(2, counter.get());

    }

}
