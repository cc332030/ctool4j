package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CBoolUtils;
import com.c332030.ctool4j.definition.function.CFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CBoolUtilsTests
 * </p>
 *
 * @since 2025/12/22
 */
public class CBoolUtilsTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void isTrue() {

        Assertions.assertTrue(CBoolUtils.isTrue(Boolean.TRUE));
        Assertions.assertFalse(CBoolUtils.isTrue(Boolean.FALSE));
        Assertions.assertFalse(CBoolUtils.isTrue(null));

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void isNotTrue() {

        Assertions.assertFalse(CBoolUtils.isNotTrue(Boolean.TRUE));
        Assertions.assertTrue(CBoolUtils.isNotTrue(Boolean.FALSE));
        Assertions.assertTrue(CBoolUtils.isNotTrue(null));

    }

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void isFalse() {

        Assertions.assertTrue(CBoolUtils.isFalse(Boolean.FALSE));
        Assertions.assertFalse(CBoolUtils.isFalse(Boolean.TRUE));
        Assertions.assertFalse(CBoolUtils.isFalse(null));

    }

    /**
     * 对应测试用例 1.4
     */
    @Test
    public void isNotFalse() {

        Assertions.assertFalse(CBoolUtils.isNotFalse(Boolean.FALSE));
        Assertions.assertTrue(CBoolUtils.isNotFalse(Boolean.TRUE));
        Assertions.assertTrue(CBoolUtils.isNotFalse(null));

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void isTrueByFunction() {

        Assertions.assertTrue(CBoolUtils.isTrue("true", Boolean::valueOf));
        Assertions.assertFalse(CBoolUtils.isTrue("false", Boolean::valueOf));
        Assertions.assertFalse(CBoolUtils.isTrue(null, (CFunction<String, Boolean>) Boolean::valueOf));

    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void isNotTrueByFunction() {

        Assertions.assertFalse(CBoolUtils.isNotTrue("true", Boolean::valueOf));
        Assertions.assertTrue(CBoolUtils.isNotTrue("false", Boolean::valueOf));
        Assertions.assertTrue(CBoolUtils.isNotTrue(null, (CFunction<String, Boolean>) Boolean::valueOf));

    }

    /**
     * 对应测试用例 2.3
     */
    @Test
    public void isFalseByFunction() {

        Assertions.assertTrue(CBoolUtils.isFalse("false", Boolean::valueOf));
        Assertions.assertFalse(CBoolUtils.isFalse("true", Boolean::valueOf));
        Assertions.assertFalse(CBoolUtils.isFalse(null, (CFunction<String, Boolean>) Boolean::valueOf));

    }

    /**
     * 对应测试用例 2.4
     */
    @Test
    public void isNotFalseByFunction() {

        Assertions.assertFalse(CBoolUtils.isNotFalse("false", Boolean::valueOf));
        Assertions.assertTrue(CBoolUtils.isNotFalse("true", Boolean::valueOf));
        Assertions.assertTrue(CBoolUtils.isNotFalse(null, (CFunction<String, Boolean>) Boolean::valueOf));

    }

}
