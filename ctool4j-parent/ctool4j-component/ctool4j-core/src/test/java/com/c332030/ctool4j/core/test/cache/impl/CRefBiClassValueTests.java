package com.c332030.ctool4j.core.test.cache.impl;

import com.c332030.ctool4j.core.cache.impl.CRefBiClassValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CRefBiClassValueTests
 * </p>
 *
 * @since 2025/12/12
 */
public class CRefBiClassValueTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void getInitial() {

        CRefBiClassValue<String> cv = CRefBiClassValue.of((t1, t2) -> t1.getSimpleName() + "-" + t2.getSimpleName());
        Assertions.assertEquals("String-Integer", cv.get(String.class, Integer.class));

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void setAndGet() {

        CRefBiClassValue<String> cv = CRefBiClassValue.of((t1, t2) -> t1.getSimpleName() + "-" + t2.getSimpleName());
        cv.set(String.class, Integer.class, "overwritten");
        Assertions.assertEquals("overwritten", cv.get(String.class, Integer.class));

    }

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void independentByPair() {

        CRefBiClassValue<String> cv = CRefBiClassValue.of((t1, t2) -> t1.getSimpleName() + "-" + t2.getSimpleName());
        cv.set(String.class, Integer.class, "str");
        Assertions.assertEquals("str", cv.get(String.class, Integer.class));
        Assertions.assertEquals("String-Long", cv.get(String.class, Long.class));

    }

}
