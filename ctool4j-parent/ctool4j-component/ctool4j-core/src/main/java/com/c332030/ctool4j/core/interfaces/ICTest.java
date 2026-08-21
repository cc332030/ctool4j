package com.c332030.ctool4j.core.interfaces;

import com.c332030.ctool4j.core.util.CTestUtils;

/**
 * <p>
 * Description: ICTest
 * </p>
 *
 * @since 2025/11/21
 * @see doc/design/core/ICTest.adoc
 * @see doc/design/core/ICTestTests.adoc
 */
public interface ICTest {

    /**
     * 是否是测试
     * @return 结果
     */
    default boolean isTest() {
        return CTestUtils.isTest();
    }

}
