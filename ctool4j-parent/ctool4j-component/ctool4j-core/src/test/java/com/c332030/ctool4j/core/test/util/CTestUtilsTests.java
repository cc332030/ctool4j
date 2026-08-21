package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CTestUtilsTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CTestUtilsTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void isTest() {

        // 测试环境 classpath 包含 JUnit，IS_TEST 恒为 true
        Assertions.assertTrue(CTestUtils.isTest());

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void isNotTest() {

        Assertions.assertFalse(CTestUtils.isNotTest());

    }

}
