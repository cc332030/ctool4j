package com.c332030.ctool4j.core.test.validation;

import com.c332030.ctool4j.core.exception.CBusinessException;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.core.validation.CAssert;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Description: CAssertTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「条件 / 相等 / null / 有效性（按类型分派）/ 空值」多个维度组织，每个断言覆盖通过（不抛）与失败（抛 CBusinessException）两分支，并验证错误信息。</li>
 *   <li>用 {@code assertThrowsExactly(CBusinessException.class, ...)} 精确匹配异常类型与信息。</li>
 *   <li>按值类型分派的断言（valid/notValid/notEmpty）逐一覆盖各参数类型的重名重载，并与 {@code CValidUtils} 的分派语义对齐。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对各断言空值/有效性语义（isEmpty/isBlank/ArrayUtil/CollUtil/MapUtil 及 {@code CValidUtils} 类型分派）与抛业务异常的约定。</li>
 *   <li>依据测试方法（等价类/边界值/异常路径）：通过/失败、String 与 Supplier 两重载、各参数类型的重名重载。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：isTrue 通过/失败及信息；equals 相等/不相等/null 组合；isNull/notNull；</li>
 *   <li>valid/notValid 各类型重载（Object/CharSequence/Iterable/Collection/Map/byte/short/char/int/long/float/double/boolean/Object 数组）；</li>
 *   <li>notEmpty 字符串/byte/int/long/Object 数组/Collection/Map；notBlank 纯空白/空/null。</li>
 *   <li>未覆盖：{@code notEmpty} 各类型的 Supplier 重载（String 重载已覆盖，Supplier 行为一致）、可迭代独立入参（其语义由 Collection 与 Map 用例覆盖）。</li>
 * </ul>
 * <h2>条件断言</h2>
 * <ul>
 *   <li>1.1 isTrue：true 通过；false 抛异常且信息正确（String 与 Supplier）（isTrue）</li>
 * </ul>
 * <h2>相等断言</h2>
 * <ul>
 *   <li>2.1 equals：相等通过；不相等抛异常；null 与值不相等；双 null 视为相等（equals）</li>
 * </ul>
 * <h2>null 断言</h2>
 * <ul>
 *   <li>3.1 isNull：null 通过；非 null 抛异常（isNull）</li>
 *   <li>3.2 notNull：非 null 通过；null 抛异常（notNull）</li>
 * </ul>
 * <h2>有效性断言（按类型分派）</h2>
 * <ul>
 *   <li>4.1 valid/notValid 对象：非 null 通过 valid、null 通过 notValid（object）</li>
 *   <li>4.2 valid/notValid 字符串：非空白通过 valid、空/空白/null 通过 notValid（charSequence）</li>
 *   <li>4.3 valid/notValid 可迭代对象：非空通过 valid、空/null 通过 notValid（iterable）</li>
 *   <li>4.4 valid/notValid 集合：非空通过 valid、空/null 通过 notValid（collection）</li>
 *   <li>4.5 valid/notValid Map：非空通过 valid、空/null 通过 notValid（map）</li>
 *   <li>4.6 valid/notValid 九种数组：非空通过 valid、空/null 通过 notValid（arrays）</li>
 * </ul>
 * <h2>空值断言</h2>
 * <ul>
 *   <li>5.1 notEmpty 字符串：非空通过；空串/null 抛异常（notEmptyString）</li>
 *   <li>5.2 notBlank：非空白通过；纯空白/空/null 抛异常（notBlank）</li>
 *   <li>5.3 notEmpty byte 数组：非空通过；空/null 抛异常（notEmptyByteArray）</li>
 *   <li>5.4 notEmpty int 数组：非空通过；空/null 抛异常（notEmptyIntArray）</li>
 *   <li>5.5 notEmpty long 数组：非空通过；空/null 抛异常（notEmptyLongArray）</li>
 *   <li>5.6 notEmpty Object 数组：非空通过；空/null 抛异常（notEmptyObjectArray）</li>
 *   <li>5.7 notEmpty 集合：非空通过；空/null 抛异常（notEmptyCollection）</li>
 *   <li>5.8 notEmpty Map：非空通过；空/null 抛异常（notEmptyMap）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.1
 */
public class CAssertTests {

    /**
     * 对应测试用例 1.1：true 通过；false 抛异常且信息正确（String 与 Supplier）
     */
    @Test
    public void isTrue() {

        // 通过
        CAssert.isTrue(true, "msg");
        CAssert.isTrue(true, () -> "msg");

        // 失败抛业务异常，且使用指定的错误信息
        val e1 = Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.isTrue(false, "error"));
        Assertions.assertEquals("error", e1.getMessage());

        val e2 = Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.isTrue(false, () -> "error2"));
        Assertions.assertEquals("error2", e2.getMessage());

    }

    /**
     * 对应测试用例 2.1：相等通过；不相等抛异常；null 与值不相等；双 null 视为相等
     */
    @Test
    public void equals() {

        CAssert.equals(1, 1, "msg");
        CAssert.equals(1, 1, () -> "msg");

        // 不相等抛异常
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.equals(1, 2, "msg"));

        // null 与值不相等（Objects.equals 语义）
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.equals(null, 1, "msg"));
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.equals(1, null, "msg"));

        // 都为 null 视为相等
        CAssert.equals(null, null, "msg");

    }

    /**
     * 对应测试用例 3.1：null 通过；非 null 抛异常
     */
    @Test
    public void isNull() {

        CAssert.isNull(null, "msg");
        CAssert.isNull(null, () -> "msg");

        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.isNull(1, "msg"));

    }

    /**
     * 对应测试用例 3.2：非 null 通过；null 抛异常
     */
    @Test
    public void notNull() {

        CAssert.notNull(1, "msg");
        CAssert.notNull(1, () -> "msg");

        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notNull(null, "msg"));

    }

    /**
     * 对应测试用例 4.1：非 null 通过 valid、null 通过 notValid；反之为失败并抛异常
     */
    @Test
    public void validObject() {

        CAssert.valid((Object) "abc", "msg");
        CAssert.valid((Object) "abc", () -> "msg");
        CAssert.notValid((Object) null, "msg");
        CAssert.notValid((Object) null, () -> "msg");

        // null 不满足 valid、非 null 不满足 notValid
        val e1 = Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.valid((Object) null, "error"));
        Assertions.assertEquals("error", e1.getMessage());

        val e2 = Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notValid((Object) "abc", () -> "error2"));
        Assertions.assertEquals("error2", e2.getMessage());

    }

    /**
     * 对应测试用例 4.2：非空白通过 valid、空/空白/null 通过 notValid（字符串按 blank 分派）
     */
    @Test
    public void validCharSequence() {

        CAssert.valid((CharSequence) "abc", "msg");
        CAssert.valid((CharSequence) "abc", () -> "msg");
        CAssert.notValid((CharSequence) "", "msg");
        CAssert.notValid((CharSequence) "   ", () -> "msg");
        CAssert.notValid((CharSequence) null, "msg");

        // 空白不满足 valid、非空白不满足 notValid
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.valid((CharSequence) "   ", "msg"));
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notValid((CharSequence) "abc", "msg"));

    }

    /**
     * 对应测试用例 4.3：非空通过 valid、空/null 通过 notValid（可迭代对象按 empty 分派）
     */
    @Test
    public void validIterable() {

        CAssert.valid((Iterable<?>) CList.of(1), "msg");
        CAssert.valid((Iterable<?>) CList.of(1), () -> "msg");
        CAssert.notValid((Iterable<?>) CList.of(), "msg");
        CAssert.notValid((Iterable<?>) null, () -> "msg");

        // 空不满足 valid、非空不满足 notValid
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.valid((Iterable<?>) CList.of(), "msg"));
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notValid((Iterable<?>) CList.of(1), "msg"));

    }

    /**
     * 对应测试用例 4.4：非空通过 valid、空/null 通过 notValid（集合按 empty 分派）
     */
    @Test
    public void validCollection() {

        CAssert.valid((Collection<?>) CList.of(1), "msg");
        CAssert.valid(CList.of(1), () -> "msg");
        CAssert.notValid((Collection<?>) CList.of(), "msg");
        CAssert.notValid((Collection<?>) null, () -> "msg");

        // 空不满足 valid、非空不满足 notValid
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.valid((Collection<?>) CList.of(), "msg"));
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notValid(CList.of(1), "msg"));

    }

    /**
     * 对应测试用例 4.5：非空通过 valid、空/null 通过 notValid（Map 按 empty 分派）
     */
    @Test
    public void validMap() {

        CAssert.valid(CMap.of("a", 1), "msg");
        CAssert.valid(CMap.of("a", 1), () -> "msg");
        CAssert.notValid((Map<?, ?>) new HashMap<>(), "msg");
        CAssert.notValid((Map<?, ?>) null, () -> "msg");

        // 空不满足 valid、非空不满足 notValid
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.valid(new HashMap<>(), "msg"));
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notValid((Map<?, ?>) CMap.of("a", 1), "msg"));

    }

    /**
     * 对应测试用例 4.6：九种数组非空通过 valid、空/null 通过 notValid（数组按 empty 分派）
     */
    @Test
    public void validArrays() {

        CAssert.valid(new byte[] {1}, "msg");
        CAssert.notValid(new byte[0], "msg");
        CAssert.notValid((byte[]) null, "msg");

        CAssert.valid(new short[] {1}, "msg");
        CAssert.notValid(new short[0], "msg");
        CAssert.notValid((short[]) null, "msg");

        CAssert.valid(new char[] {'a'}, "msg");
        CAssert.notValid(new char[0], "msg");
        CAssert.notValid((char[]) null, "msg");

        CAssert.valid(new int[] {1}, "msg");
        CAssert.notValid(new int[0], "msg");
        CAssert.notValid((int[]) null, "msg");

        CAssert.valid(new long[] {1L}, "msg");
        CAssert.notValid(new long[0], "msg");
        CAssert.notValid((long[]) null, "msg");

        CAssert.valid(new float[] {1f}, "msg");
        CAssert.notValid(new float[0], "msg");
        CAssert.notValid((float[]) null, "msg");

        CAssert.valid(new double[] {1d}, "msg");
        CAssert.notValid(new double[0], "msg");
        CAssert.notValid((double[]) null, "msg");

        CAssert.valid(new boolean[] {true}, "msg");
        CAssert.notValid(new boolean[0], "msg");
        CAssert.notValid((boolean[]) null, "msg");

        CAssert.valid(new Object[] {"a"}, "msg");
        CAssert.notValid(new Object[0], "msg");
        CAssert.notValid((Object[]) null, "msg");

        // 空不满足 valid、非空不满足 notValid（String/Supplier 两重载各验一类）
        val e1 = Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.valid(new int[0], "error"));
        Assertions.assertEquals("error", e1.getMessage());

        val e2 = Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notValid(new int[] {1}, () -> "error2"));
        Assertions.assertEquals("error2", e2.getMessage());

    }

    /**
     * 对应测试用例 5.1：notEmpty 字符串：非空通过；空串/null 抛异常
     */
    @Test
    public void notEmptyString() {

        CAssert.notEmpty("a", "msg");
        CAssert.notEmpty("a", () -> "msg");

        // 空字符串抛异常
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty("", "msg"));

        // null 抛异常
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty((String) null, "msg"));

    }

    /**
     * 对应测试用例 5.2：非空白通过；纯空白/空/null 抛异常
     */
    @Test
    public void notBlank() {

        CAssert.notBlank(" a ", "msg");
        CAssert.notBlank(" a ", () -> "msg");

        // 纯空白抛异常
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notBlank(" ", "msg"));

        // 空字符串 / null 抛异常
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notBlank("", "msg"));
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notBlank(null, "msg"));

    }

    /**
     * 对应测试用例 5.3：notEmpty byte 数组：非空通过；空/null 抛异常
     */
    @Test
    public void notEmptyByteArray() {

        CAssert.notEmpty(new byte[] {1}, "msg");

        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty(new byte[0], "msg"));
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty((byte[]) null, "msg"));

    }

    /**
     * 对应测试用例 5.4：notEmpty int 数组：非空通过；空/null 抛异常
     */
    @Test
    public void notEmptyIntArray() {

        CAssert.notEmpty(new int[] {1}, "msg");

        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty(new int[0], "msg"));
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty((int[]) null, "msg"));

    }

    /**
     * 对应测试用例 5.5：notEmpty long 数组：非空通过；空/null 抛异常
     */
    @Test
    public void notEmptyLongArray() {

        CAssert.notEmpty(new long[] {1L}, "msg");

        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty(new long[0], "msg"));
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty((long[]) null, "msg"));

    }

    /**
     * 对应测试用例 5.6：notEmpty Object 数组：非空通过；空/null 抛异常
     */
    @Test
    public void notEmptyObjectArray() {

        CAssert.notEmpty(new Object[] {1}, "msg");

        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty(new Object[0], "msg"));
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty((Object[]) null, "msg"));

    }

    /**
     * 对应测试用例 5.7：notEmpty 集合：非空通过；空/null 抛异常
     */
    @Test
    public void notEmptyCollection() {

        CAssert.notEmpty(CList.of(1), "msg");

        // 空集合 / null 抛异常
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty(CList.of(), "msg"));
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty((Collection<?>) null, "msg"));

    }

    /**
     * 对应测试用例 5.8：notEmpty Map：非空通过；空/null 抛异常
     */
    @Test
    public void notEmptyMap() {

        CAssert.notEmpty(CMap.of("a", 1), "msg");

        // 空 map / null 抛异常
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty(new HashMap<>(), "msg"));
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty((Map<?, ?>) null, "msg"));

    }

}
