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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「对象 / 字符串 / 集合 / Map / 数组」多个类型维度组织，每类覆盖 isValid 与 isNotValid 的正反分支。</li>
 *   <li>对象验证非 null；字符串验证空白；集合/Map/数组验证非空。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对按类型（notNull/notBlank/notEmpty）的校验约定。</li>
 *   <li>依据测试方法（等价类/边界值）：正反分支、空/null/空白。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：对象（非 null/null）；字符串（非空白/空/空白/null）；集合（非空/空）；Map（非空/空）；</li>
 *   <li>九种数组（byte/short/char/int/long/float/double/boolean/Object 非空/空）。</li>
 *   <li>未覆盖：Iterable 独立入参（被 Collection 重载语义覆盖）；各数组的 isNotValid 反例分支</li>
 *   <li>（arrays 测试覆盖 isValid 与 isNotValid 空分支，非空分支由 isValid 覆盖）。</li>
 * </ul>
 * <h2>对象（非 null）</h2>
 * <ul>
 *   <li>1.1 isValid/isNotValid：非 null 有效、null 无效（object）</li>
 * </ul>
 * <h2>字符串（notBlank）</h2>
 * <ul>
 *   <li>2.1 isValid/isNotValid：非空白有效、空/空白/null 无效（charSequence）</li>
 * </ul>
 * <h2>集合（notEmpty）</h2>
 * <ul>
 *   <li>3.1 isValid/isNotValid：非空有效、空无效（collection）</li>
 * </ul>
 * <h2>Map（notEmpty）</h2>
 * <ul>
 *   <li>4.1 isValid/isNotValid：非空有效、空无效（map）</li>
 * </ul>
 * <h2>数组（notEmpty）</h2>
 * <ul>
 *   <li>5.1 九种数组：非空有效、空无效（arrays）</li>
 * </ul>
 *
 * @since 2026/8/20
 * @version 1.0
 */
public class CValidUtilsTests {

    // ---------- Object（非 null） ----------

    /**
     * 对应测试用例 1.1：isValid/isNotValid：非 null 有效、null 无效
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
     * 对应测试用例 2.1：isValid/isNotValid：非空白有效、空/空白/null 无效
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
     * 对应测试用例 3.1：isValid/isNotValid：非空有效、空无效
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
     * 对应测试用例 4.1：isValid/isNotValid：非空有效、空无效
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
     * 对应测试用例 5.1：九种数组：非空有效、空无效
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
