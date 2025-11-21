package com.c332030.ctool4j.core.util;

import com.c332030.ctool4j.definition.constant.CToolTestConstants;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * Description: CTestUtils
 * </p>
 *
 * @since 2025/11/21
 */
@Slf4j
@UtilityClass
public class CTestUtils {

    /**
     * 是否是测试
     * @return 结果
     */
    public boolean isTest() {
        return CToolTestConstants.IS_TEST;
    }

    /**
     * 是否不是测试
     * @return 结果
     */
    public boolean isNotTest() {
        return !isTest();
    }

}
