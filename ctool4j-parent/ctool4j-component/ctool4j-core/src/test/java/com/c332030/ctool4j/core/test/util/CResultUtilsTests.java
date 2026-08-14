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

    @Test
    public void successCodes() {

        Assertions.assertTrue(CResultUtils.SUCCESS_CODES.contains("0"));
        Assertions.assertTrue(CResultUtils.SUCCESS_CODES.contains("200"));
        Assertions.assertTrue(CResultUtils.SUCCESS_CODES.contains("000000"));

    }

    @Test
    public void isSuccess() {

        Assertions.assertTrue(CResultUtils.isSuccess(CStrResult.success("data")));
        Assertions.assertFalse(CResultUtils.isSuccess(CStrResult.error("500", "error")));
        Assertions.assertFalse(CResultUtils.isSuccess(null));

    }

    @Test
    public void isNotSuccess() {

        Assertions.assertFalse(CResultUtils.isNotSuccess(CStrResult.success("data")));
        Assertions.assertTrue(CResultUtils.isNotSuccess(CStrResult.error("500", "error")));
        Assertions.assertTrue(CResultUtils.isNotSuccess(null));

    }

    @Test
    public void assertSuccessOk() {

        Assertions.assertDoesNotThrow(() -> CResultUtils.assertSuccess(CStrResult.success("data")));

    }

    @Test
    public void assertSuccessErrorThrows() {

        Assertions.assertThrowsExactly(CBusinessException.class,
                () -> CResultUtils.assertSuccess(CStrResult.error("500", "boom")));

    }

    @Test
    public void assertSuccessNullThrows() {

        Assertions.assertThrowsExactly(CBusinessException.class,
                () -> CResultUtils.assertSuccess(null));

    }

    @Test
    public void getData() {

        Assertions.assertEquals("data", CResultUtils.getData(CStrResult.success("data")));

    }

    @Test
    public void getDataWithDefault() {

        Assertions.assertEquals("data", CResultUtils.getData(CStrResult.success("data"), "default"));
        Assertions.assertEquals("default", CResultUtils.getData(CStrResult.success(null), "default"));

    }

    @Test
    public void getDataErrorThrows() {

        Assertions.assertThrowsExactly(CBusinessException.class,
                () -> CResultUtils.getData(CStrResult.error("500", "boom")));

    }

    @Test
    public void getDataNullThrows() {

        Assertions.assertThrowsExactly(CBusinessException.class,
                () -> CResultUtils.getData(null));

    }

    @Test
    public void getDataDefaultEmptyList() {

        Assertions.assertEquals(Arrays.asList("a", "b"),
                CResultUtils.getDataDefaultEmptyList(CStrResult.success(Arrays.asList("a", "b"))));
        Assertions.assertEquals(Collections.emptyList(),
                CResultUtils.getDataDefaultEmptyList(CStrResult.success(null)));

    }

}
