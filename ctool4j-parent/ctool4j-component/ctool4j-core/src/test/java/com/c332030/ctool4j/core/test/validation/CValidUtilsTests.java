package com.c332030.ctool4j.core.test.validation;

import com.c332030.ctool4j.core.validation.CValidUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

/**
 * <p>
 * Description: CValidUtils 按类型校验逻辑测试
 * </p>
 *
 * <p>
 * 覆盖：对象（notNull）、字符串（notBlank）、集合（notEmpty）、Map（notEmpty）、各类型数组（notEmpty）
 * </p>
 *
 * @since 2026/8/20
 */
public class CValidUtilsTests {

    // ---------- Object（非 null） ----------

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void object() {
        Assertions.assertTrue(CValidUtils.isValid((Object) "abc"));
        Assertions.assertFalse(CValidUtils.isValid((Object) null));
        Assertions.assertTrue(CValidUtils.isNotValid((Object) null));
        Assertions.assertFalse(CValidUtils.isNotValid((Object) "abc"));
    }

    // ---------- CharSequence（notBlank） ----------

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void charSequence() {
        Assertions.assertTrue(CValidUtils.isValid("abc"));
        Assertions.assertFalse(CValidUtils.isValid(""));
        Assertions.assertFalse(CValidUtils.isValid("   "));
        Assertions.assertTrue(CValidUtils.isNotValid("   "));
        Assertions.assertTrue(CValidUtils.isNotValid((CharSequence) null));
        Assertions.assertFalse(CValidUtils.isNotValid("abc"));
    }

    // ---------- Collection / Iterable（notEmpty） ----------

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void collection() {
        Assertions.assertTrue(CValidUtils.isValid(Collections.singletonList(1)));
        Assertions.assertFalse(CValidUtils.isValid(Collections.emptyList()));
        Assertions.assertTrue(CValidUtils.isNotValid(Collections.emptyList()));
        Assertions.assertFalse(CValidUtils.isNotValid(Collections.singletonList(1)));
    }

    // ---------- Map（notEmpty） ----------

    /**
     * 对应测试用例 4.1
     */
    @Test
    public void map() {
        Assertions.assertTrue(CValidUtils.isValid(Collections.singletonMap("k", "v")));
        Assertions.assertFalse(CValidUtils.isValid(Collections.emptyMap()));
        Assertions.assertTrue(CValidUtils.isNotValid(Collections.emptyMap()));
        Assertions.assertFalse(CValidUtils.isNotValid(Collections.singletonMap("k", "v")));
    }

    // ---------- 数组（notEmpty） ----------

    /**
     * 对应测试用例 5.1
     */
    @Test
    public void arrays() {
        Assertions.assertTrue(CValidUtils.isValid(new byte[]{1}));
        Assertions.assertFalse(CValidUtils.isValid(new byte[0]));
        Assertions.assertTrue(CValidUtils.isNotValid(new byte[0]));

        Assertions.assertTrue(CValidUtils.isValid(new short[]{1}));
        Assertions.assertFalse(CValidUtils.isValid(new short[0]));
        Assertions.assertTrue(CValidUtils.isNotValid(new short[0]));

        Assertions.assertTrue(CValidUtils.isValid(new char[]{'a'}));
        Assertions.assertFalse(CValidUtils.isValid(new char[0]));
        Assertions.assertTrue(CValidUtils.isNotValid(new char[0]));

        Assertions.assertTrue(CValidUtils.isValid(new int[]{1}));
        Assertions.assertFalse(CValidUtils.isValid(new int[0]));
        Assertions.assertTrue(CValidUtils.isNotValid(new int[0]));

        Assertions.assertTrue(CValidUtils.isValid(new long[]{1L}));
        Assertions.assertFalse(CValidUtils.isValid(new long[0]));
        Assertions.assertTrue(CValidUtils.isNotValid(new long[0]));

        Assertions.assertTrue(CValidUtils.isValid(new Object[]{"a"}));
        Assertions.assertFalse(CValidUtils.isValid(new Object[0]));
        Assertions.assertTrue(CValidUtils.isNotValid(new Object[0]));

        Assertions.assertTrue(CValidUtils.isValid(new float[]{1f}));
        Assertions.assertFalse(CValidUtils.isValid(new float[0]));
        Assertions.assertTrue(CValidUtils.isNotValid(new float[0]));

        Assertions.assertTrue(CValidUtils.isValid(new double[]{1d}));
        Assertions.assertFalse(CValidUtils.isValid(new double[0]));
        Assertions.assertTrue(CValidUtils.isNotValid(new double[0]));

        Assertions.assertTrue(CValidUtils.isValid(new boolean[]{true}));
        Assertions.assertFalse(CValidUtils.isValid(new boolean[0]));
        Assertions.assertTrue(CValidUtils.isNotValid(new boolean[0]));
    }

}
