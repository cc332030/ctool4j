package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.exception.CBusinessExceptionProvider;
import com.c332030.ctool4j.core.exception.ICBusinessExceptionProvider;
import com.c332030.ctool4j.core.util.CSpiUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

/**
 * <p>
 * Description: CSpiUtilsTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CSpiUtilsTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void getImpls() {

        List<ICBusinessExceptionProvider> impls = CSpiUtils.getImpls(ICBusinessExceptionProvider.class);

        Assertions.assertEquals(1, impls.size());
        Assertions.assertInstanceOf(CBusinessExceptionProvider.class, impls.get(0));

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void getImplsNoProvider() {

        Assertions.assertTrue(CSpiUtils.getImpls(String.class).isEmpty());

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void getFirstImpl() {

        ICBusinessExceptionProvider impl = CSpiUtils.getFirstImpl(ICBusinessExceptionProvider.class);
        Assertions.assertInstanceOf(CBusinessExceptionProvider.class, impl);

    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void getFirstImplNoProviderThrows() {

        Assertions.assertThrowsExactly(IllegalStateException.class,
                () -> CSpiUtils.getFirstImpl(String.class));

    }

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void getFirstCustomImplOrDefault() {

        ICBusinessExceptionProvider impl = CSpiUtils.getFirstCustomImplOrDefault(
                ICBusinessExceptionProvider.class, CBusinessExceptionProvider.class);

        Assertions.assertInstanceOf(CBusinessExceptionProvider.class, impl);

    }

    /**
     * 对应测试用例 4.1
     */
    @Test
    public void getImplsSorted() {

        List<ICBusinessExceptionProvider> impls = CSpiUtils.getImplsSorted(
                ICBusinessExceptionProvider.class,
                Comparator.comparingInt(o -> 0));

        Assertions.assertEquals(1, impls.size());
        Assertions.assertInstanceOf(CBusinessExceptionProvider.class, impls.get(0));

    }

}
