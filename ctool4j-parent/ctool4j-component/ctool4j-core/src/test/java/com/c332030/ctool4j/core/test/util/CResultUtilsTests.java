package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.exception.CBusinessException;
import com.c332030.ctool4j.core.util.CResultUtils;
import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * <p>
 * Description: CResultUtilsTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CResultUtilsTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void successCodes() {

        Assertions.assertTrue(CResultUtils.SUCCESS_CODES.contains("0"));
        Assertions.assertTrue(CResultUtils.SUCCESS_CODES.contains("200"));
        Assertions.assertTrue(CResultUtils.SUCCESS_CODES.contains("000000"));

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void isSuccess() {

        Assertions.assertTrue(CResultUtils.isSuccess(CStrResult.success("data")));
        Assertions.assertFalse(CResultUtils.isSuccess(CStrResult.error("500", "error")));
        Assertions.assertFalse(CResultUtils.isSuccess(null));

    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void isNotSuccess() {

        Assertions.assertFalse(CResultUtils.isNotSuccess(CStrResult.success("data")));
        Assertions.assertTrue(CResultUtils.isNotSuccess(CStrResult.error("500", "error")));
        Assertions.assertTrue(CResultUtils.isNotSuccess(null));

    }

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void assertSuccessOk() {

        Assertions.assertDoesNotThrow(() -> CResultUtils.assertSuccess(CStrResult.success("data")));

    }

    /**
     * 对应测试用例 3.2
     */
    @Test
    public void assertSuccessErrorThrows() {

        Assertions.assertThrowsExactly(CBusinessException.class,
                () -> CResultUtils.assertSuccess(CStrResult.error("500", "boom")));

    }

    /**
     * 对应测试用例 3.3
     */
    @Test
    public void assertSuccessNullThrows() {

        Assertions.assertThrowsExactly(CBusinessException.class,
                () -> CResultUtils.assertSuccess(null));

    }

    /**
     * 对应测试用例 4.1
     */
    @Test
    public void getData() {

        Assertions.assertEquals("data", CResultUtils.getData(CStrResult.success("data")));

    }

    /**
     * 对应测试用例 4.2
     */
    @Test
    public void getDataWithDefault() {

        Assertions.assertEquals("data", CResultUtils.getData(CStrResult.success("data"), "default"));
        Assertions.assertEquals("default", CResultUtils.getData(CStrResult.success(null), "default"));

    }

    /**
     * 对应测试用例 4.3
     */
    @Test
    public void getDataErrorThrows() {

        Assertions.assertThrowsExactly(CBusinessException.class,
                () -> CResultUtils.getData(CStrResult.error("500", "boom")));

    }

    /**
     * 对应测试用例 4.4
     */
    @Test
    public void getDataNullThrows() {

        Assertions.assertThrowsExactly(CBusinessException.class,
                () -> CResultUtils.getData(null));

    }

    /**
     * 对应测试用例 4.5
     */
    @Test
    public void getDataDefaultEmptyList() {

        Assertions.assertEquals(Arrays.asList("a", "b"),
                CResultUtils.getDataDefaultEmptyList(CStrResult.success(Arrays.asList("a", "b"))));
        Assertions.assertEquals(Collections.emptyList(),
                CResultUtils.getDataDefaultEmptyList(CStrResult.success(null)));

    }

}
