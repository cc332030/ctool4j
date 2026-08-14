package com.c332030.ctool4j.core.test.cache.impl;

import com.c332030.ctool4j.core.cache.impl.CRefClassValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CRefClassValueTests
 * </p>
 *
 * @since 2025/12/12
 */
public class CRefClassValueTests {

    @Test
    public void getInitial() {

        CRefClassValue<String> cv = CRefClassValue.of(clazz -> clazz.getSimpleName());
        Assertions.assertEquals("String", cv.get(String.class));

    }

    @Test
    public void setAndGet() {

        CRefClassValue<String> cv = CRefClassValue.of(clazz -> clazz.getSimpleName());
        cv.set(String.class, "overwritten");
        Assertions.assertEquals("overwritten", cv.get(String.class));

    }

    @Test
    public void independentByClass() {

        CRefClassValue<String> cv = CRefClassValue.of(clazz -> clazz.getSimpleName());
        cv.set(String.class, "str");
        Assertions.assertEquals("str", cv.get(String.class));
        Assertions.assertEquals("Integer", cv.get(Integer.class));

    }

}
