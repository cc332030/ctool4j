package com.c332030.ctool4j.core.util;

import com.c332030.ctool4j.definition.constant.CTool4jTestConstants;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;

/**
 * <p>
 * Description: CTestUtils
 * </p>
 *
 * @since 2025/11/21
 * @see "doc/design/core/CTestUtils.adoc"
 * @see "doc/design/core/CTestUtilsTests.adoc"
 */
@CustomLog
@UtilityClass
public class CTestUtils {

    /**
     * 是否是测试
     * @return 结果
     */
    public boolean isTest() {
        return CTool4jTestConstants.IS_TEST;
    }

    /**
     * 是否不是测试
     * @return 结果
     */
    public boolean isNotTest() {
        return !isTest();
    }

}
