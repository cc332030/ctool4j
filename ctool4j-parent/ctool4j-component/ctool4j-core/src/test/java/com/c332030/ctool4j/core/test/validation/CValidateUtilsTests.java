package com.c332030.ctool4j.core.test.validation;

import com.c332030.ctool4j.core.validation.CValidateUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Description: CValidateUtilsTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「null / 字符串 / 可迭代集合 / Map / 数组」多个类型维度组织，每类覆盖 isEmpty 与 isNotEmpty 正反分支。</li>
 *   <li>null 覆盖 isNull/isNotNull；字符串覆盖 empty/notEmpty/blank/notBlank；集合/Map/数组覆盖 empty/notEmpty。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对各类型空值判断（isEmpty/isBlank）的约定。</li>
 *   <li>依据测试方法（等价类/边界值）：null/空/非空、空串/空白。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：isNull/isNotNull；字符串 empty/notEmpty/blank/notBlank；可迭代 isEmpty/isNotEmpty；</li>
 *   <li>集合 isEmpty/isNotEmpty；Map isEmpty/isNotEmpty；byte/short/char/int/long/Object 数组 isEmpty/isNotEmpty。</li>
 *   <li>未覆盖：float/double/boolean 数组（本类未提供对应重载，属设计边界）。</li>
 * </ul>
 * <h2>null 判断</h2>
 * <ul>
 *   <li>1.1 isNull：null 为 true、非 null 为 false（isNull）</li>
 *   <li>1.2 isNotNull：非 null 为 true、null 为 false（isNotNull）</li>
 * </ul>
 * <h2>字符串</h2>
 * <ul>
 *   <li>2.1 isEmpty：null/空串为 true、非空为 false（isEmptyCharSequence）</li>
 *   <li>2.2 isNotEmpty：null/空串为 false、非空为 true（isNotEmptyCharSequence）</li>
 *   <li>2.3 isBlank：null/空串/纯空白为 true、非空白为 false（isBlank）</li>
 *   <li>2.4 isNotBlank：null/纯空白为 false、非空白为 true（isNotBlank）</li>
 * </ul>
 * <h2>可迭代与集合</h2>
 * <ul>
 *   <li>3.1 isEmpty 集合：null/空为 true、非空为 false（isEmptyCollection）</li>
 *   <li>3.2 isNotEmpty 集合：null 为 false、非空为 true（isNotEmptyCollection）</li>
 *   <li>3.3 isEmpty 可迭代：null/空为 true、非空为 false（isEmptyIterable）</li>
 *   <li>3.4 isNotEmpty 可迭代：null 为 false、非空为 true（isNotEmptyIterable）</li>
 * </ul>
 * <h2>Map</h2>
 * <ul>
 *   <li>4.1 isEmpty：null/空为 true、非空为 false（isEmptyMap）</li>
 *   <li>4.2 isNotEmpty：null 为 false、非空为 true（isNotEmptyMap）</li>
 * </ul>
 * <h2>数组</h2>
 * <ul>
 *   <li>5.1 byte：isEmpty/isNotEmpty（isEmptyByteArray / isNotEmptyByteArray）</li>
 *   <li>5.2 short：isEmpty/isNotEmpty（isEmptyShortArray / isNotEmptyShortArray）</li>
 *   <li>5.3 char：isEmpty/isNotEmpty（isEmptyCharArray / isNotEmptyCharArray）</li>
 *   <li>5.4 int：isEmpty/isNotEmpty（isEmptyIntArray / isNotEmptyIntArray）</li>
 *   <li>5.5 long：isEmpty/isNotEmpty（isEmptyLongArray / isNotEmptyLongArray）</li>
 *   <li>5.6 Object：isEmpty/isNotEmpty（isEmptyObjectArray / isNotEmptyObjectArray）</li>
 * </ul>
 *
 * @since 2025/12/12
 * @version 1.0
 */
public class CValidateUtilsTests {

    // ---- isNull / isNotNull ----

    /**
     * 对应测试用例 1.1：null 为 true、非 null 为 false
     */
    @Test
    public void isNull() {

        Assertions.assertTrue(CValidateUtils.isNull(null));
        Assertions.assertFalse(CValidateUtils.isNull("x"));

    }

    /**
     * 对应测试用例 1.2：非 null 为 true、null 为 false
     */
    @Test
    public void isNotNull() {

        Assertions.assertTrue(CValidateUtils.isNotNull("x"));
        Assertions.assertFalse(CValidateUtils.isNotNull(null));

    }

    // ---- isEmpty(CharSequence) ----

    /**
     * 对应测试用例 2.1：null/空串为 true、非空为 false
     */
    @Test
    public void isEmptyCharSequence() {

        Assertions.assertTrue(CValidateUtils.isEmpty((CharSequence) null));
        Assertions.assertTrue(CValidateUtils.isEmpty(""));
        Assertions.assertFalse(CValidateUtils.isEmpty("a"));

    }

    /**
     * 对应测试用例 2.2：null/空串为 false、非空为 true
     */
    @Test
    public void isNotEmptyCharSequence() {

        Assertions.assertFalse(CValidateUtils.isNotEmpty((CharSequence) null));
        Assertions.assertFalse(CValidateUtils.isNotEmpty(""));
        Assertions.assertTrue(CValidateUtils.isNotEmpty("a"));

    }

    // ---- isBlank ----

    /**
     * 对应测试用例 2.3：null/空串/纯空白为 true、非空白为 false
     */
    @Test
    public void isBlank() {

        Assertions.assertTrue(CValidateUtils.isBlank(null));
        Assertions.assertTrue(CValidateUtils.isBlank(""));
        Assertions.assertTrue(CValidateUtils.isBlank("   "));
        Assertions.assertFalse(CValidateUtils.isBlank("a"));

    }

    /**
     * 对应测试用例 2.4：null/纯空白为 false、非空白为 true
     */
    @Test
    public void isNotBlank() {

        Assertions.assertFalse(CValidateUtils.isNotBlank(null));
        Assertions.assertFalse(CValidateUtils.isNotBlank("   "));
        Assertions.assertTrue(CValidateUtils.isNotBlank("a"));

    }

    // ---- isEmpty(Iterable / Collection) ----

    /**
     * 对应测试用例 3.1：isEmpty 集合：null/空为 true、非空为 false
     */
    @Test
    public void isEmptyCollection() {

        Assertions.assertTrue(CValidateUtils.isEmpty((Collection<Object>) null));
        Assertions.assertTrue(CValidateUtils.isEmpty(Collections.emptyList()));
        Assertions.assertFalse(CValidateUtils.isEmpty(Collections.singletonList("a")));

    }

    /**
     * 对应测试用例 3.2：isNotEmpty 集合：null 为 false、非空为 true
     */
    @Test
    public void isNotEmptyCollection() {

        Assertions.assertFalse(CValidateUtils.isNotEmpty((Collection<Object>) null));
        Assertions.assertTrue(CValidateUtils.isNotEmpty(Collections.singletonList("a")));

    }

    /**
     * 对应测试用例 3.3：isEmpty 可迭代：null/空为 true、非空为 false
     */
    @Test
    public void isEmptyIterable() {

        Assertions.assertTrue(CValidateUtils.isEmpty((Iterable<Object>) null));
        Assertions.assertTrue(CValidateUtils.isEmpty(Collections.emptyList()));
        Assertions.assertFalse(CValidateUtils.isEmpty(Collections.singletonList("a")));

    }

    /**
     * 对应测试用例 3.4：isNotEmpty 可迭代：null 为 false、非空为 true
     */
    @Test
    public void isNotEmptyIterable() {

        Assertions.assertFalse(CValidateUtils.isNotEmpty((Iterable<Object>) null));
        Assertions.assertTrue(CValidateUtils.isNotEmpty(Collections.singletonList("a")));

    }

    // ---- isEmpty(Map) ----

    /**
     * 对应测试用例 4.1：null/空为 true、非空为 false
     */
    @Test
    public void isEmptyMap() {

        Assertions.assertTrue(CValidateUtils.isEmpty((Map<Object, Object>) null));
        Assertions.assertTrue(CValidateUtils.isEmpty(new HashMap<>()));

        Map<String, String> map = new HashMap<>();
        map.put("a", "1");
        Assertions.assertFalse(CValidateUtils.isEmpty(map));

    }

    /**
     * 对应测试用例 4.2：null 为 false、非空为 true
     */
    @Test
    public void isNotEmptyMap() {

        Assertions.assertFalse(CValidateUtils.isNotEmpty((Map<Object, Object>) null));
        Map<String, String> map = new HashMap<>();
        map.put("a", "1");
        Assertions.assertTrue(CValidateUtils.isNotEmpty(map));

    }

    // ---- isEmpty(数组) ----

    /**
     * 对应测试用例 5.1：isEmpty/isNotEmpty（isEmptyByteArray / isNotEmptyByteArray）
     */
    @Test
    public void isEmptyByteArray() {

        Assertions.assertTrue(CValidateUtils.isEmpty((byte[]) null));
        Assertions.assertTrue(CValidateUtils.isEmpty(new byte[0]));
        Assertions.assertFalse(CValidateUtils.isEmpty(new byte[] {1}));

    }

    /**
     * 对应测试用例 5.1：isEmpty/isNotEmpty（isEmptyByteArray / isNotEmptyByteArray）
     */
    @Test
    public void isNotEmptyByteArray() {

        Assertions.assertFalse(CValidateUtils.isNotEmpty((byte[]) null));
        Assertions.assertTrue(CValidateUtils.isNotEmpty(new byte[] {1}));

    }

    /**
     * 对应测试用例 5.2：isEmpty/isNotEmpty（isEmptyShortArray / isNotEmptyShortArray）
     */
    @Test
    public void isEmptyShortArray() {

        Assertions.assertTrue(CValidateUtils.isEmpty((short[]) null));
        Assertions.assertTrue(CValidateUtils.isEmpty(new short[0]));
        Assertions.assertFalse(CValidateUtils.isEmpty(new short[] {1}));

    }

    /**
     * 对应测试用例 5.2：isEmpty/isNotEmpty（isEmptyShortArray / isNotEmptyShortArray）
     */
    @Test
    public void isNotEmptyShortArray() {

        Assertions.assertFalse(CValidateUtils.isNotEmpty((short[]) null));
        Assertions.assertTrue(CValidateUtils.isNotEmpty(new short[] {1}));

    }

    /**
     * 对应测试用例 5.3：isEmpty/isNotEmpty（isEmptyCharArray / isNotEmptyCharArray）
     */
    @Test
    public void isEmptyCharArray() {

        Assertions.assertTrue(CValidateUtils.isEmpty((char[]) null));
        Assertions.assertTrue(CValidateUtils.isEmpty(new char[0]));
        Assertions.assertFalse(CValidateUtils.isEmpty(new char[] {'a'}));

    }

    /**
     * 对应测试用例 5.3：isEmpty/isNotEmpty（isEmptyCharArray / isNotEmptyCharArray）
     */
    @Test
    public void isNotEmptyCharArray() {

        Assertions.assertFalse(CValidateUtils.isNotEmpty((char[]) null));
        Assertions.assertTrue(CValidateUtils.isNotEmpty(new char[] {'a'}));

    }

    /**
     * 对应测试用例 5.4：isEmpty/isNotEmpty（isEmptyIntArray / isNotEmptyIntArray）
     */
    @Test
    public void isEmptyIntArray() {

        Assertions.assertTrue(CValidateUtils.isEmpty((int[]) null));
        Assertions.assertTrue(CValidateUtils.isEmpty(new int[0]));
        Assertions.assertFalse(CValidateUtils.isEmpty(new int[] {1}));

    }

    /**
     * 对应测试用例 5.4：isEmpty/isNotEmpty（isEmptyIntArray / isNotEmptyIntArray）
     */
    @Test
    public void isNotEmptyIntArray() {

        Assertions.assertFalse(CValidateUtils.isNotEmpty((int[]) null));
        Assertions.assertTrue(CValidateUtils.isNotEmpty(new int[] {1}));

    }

    /**
     * 对应测试用例 5.5：isEmpty/isNotEmpty（isEmptyLongArray / isNotEmptyLongArray）
     */
    @Test
    public void isEmptyLongArray() {

        Assertions.assertTrue(CValidateUtils.isEmpty((long[]) null));
        Assertions.assertTrue(CValidateUtils.isEmpty(new long[0]));
        Assertions.assertFalse(CValidateUtils.isEmpty(new long[] {1L}));

    }

    /**
     * 对应测试用例 5.5：isEmpty/isNotEmpty（isEmptyLongArray / isNotEmptyLongArray）
     */
    @Test
    public void isNotEmptyLongArray() {

        Assertions.assertFalse(CValidateUtils.isNotEmpty((long[]) null));
        Assertions.assertTrue(CValidateUtils.isNotEmpty(new long[] {1L}));

    }

    /**
     * 对应测试用例 5.6：isEmpty/isNotEmpty（isEmptyObjectArray / isNotEmptyObjectArray）
     */
    @Test
    public void isEmptyObjectArray() {

        Assertions.assertTrue(CValidateUtils.isEmpty((Object[]) null));
        Assertions.assertTrue(CValidateUtils.isEmpty(new Object[0]));
        Assertions.assertFalse(CValidateUtils.isEmpty(new Object[] {"a"}));

    }

    /**
     * 对应测试用例 5.6：isEmpty/isNotEmpty（isEmptyObjectArray / isNotEmptyObjectArray）
     */
    @Test
    public void isNotEmptyObjectArray() {

        Assertions.assertFalse(CValidateUtils.isNotEmpty((Object[]) null));
        Assertions.assertTrue(CValidateUtils.isNotEmpty(new Object[] {"a"}));

    }

}
