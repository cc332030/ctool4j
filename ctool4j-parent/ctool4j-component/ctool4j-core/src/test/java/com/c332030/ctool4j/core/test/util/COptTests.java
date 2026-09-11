package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.core.util.COpt;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: COptTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「构造 / 判断 / 取值 / 转换」多个维度组织。</li>
 *   <li>构造覆盖 of（null 抛 NPE）、empty 与 ofNullable 等价、ofEmptyAble 三类空判定（集合/Map/字符串）、</li>
 *   <li>ofBlankAble 空白。</li>
 *   <li>取值覆盖 get、orElse、orElseGet（含惰性验证）、orElseThrow。</li>
 *   <li>转换覆盖 map、flatMap。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对构造空判定、get 抛 NoSuchElementException、orElse 惰性、map/flatMap null 语义的约定。</li>
 *   <li>依据测试方法（等价类/边界值/分支覆盖）：null、空集合/Map/字符串、空白、惰性 supplier。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：of null 抛 NPE；empty/ofNullable 等价；ofEmptyAble（空/非空集合、空/非空 Map、空/非空白字符串）；</li>
 *   <li>ofBlankAble（空白/非空白）；isPresent 正反；get 取值；orElse/orElseGet（含惰性）/orElseThrow；</li>
 *   <li>map/flatMap 转换。</li>
 *   <li>未覆盖：filter、ifPresent、orElseThrow 非空不抛的分支、flatMap 返回 null 的分支（当前测试聚焦</li>
 *   <li>核心构造与取值路径，可后续补充）。</li>
 * </ul>
 * <h2>构造</h2>
 * <ul>
 *   <li>1.1 of：null 抛 NullPointerException（of）</li>
 *   <li>1.2 empty 与 ofNullable(null) 等价（empty）</li>
 *   <li>1.3 ofEmptyAble：空集合/空 Map/空字符串 → 空；非空集合/Map/非空白字符串 → 有值（ofEmptyAble）</li>
 *   <li>1.4 ofBlankAble：空白 {@code " "} → 空；非空白 {@code "1"} → 有值（ofBlankAble）</li>
 * </ul>
 * <h2>判断</h2>
 * <ul>
 *   <li>2.1 isPresent：非空为 true、null 为 false（isPresent）</li>
 * </ul>
 * <h2>取值</h2>
 * <ul>
 *   <li>3.1 get：非空取值正确（get）</li>
 *   <li>3.2 orElse：空值返回默认值（orElse）</li>
 *   <li>3.3 orElseGet：空值经 supplier 取默认值；值存在时不调用 supplier（惰性）（orElseGet）</li>
 *   <li>3.4 orElseThrow：空值抛指定异常（orElseThrow）</li>
 * </ul>
 * <h2>转换</h2>
 * <ul>
 *   <li>4.1 map：值映射后取值正确（map）</li>
 *   <li>4.2 flatMap：扁平化映射后取值正确（flatMap）</li>
 * </ul>
 *
 * @since 2025/12/6
 * @version 1.0
 */
public class COptTests {

    /**
     * 测试 of 对 null 抛 NPE
     * 对应测试用例 1.1：null 抛 NullPointerException
     */
    @Test
    public void of() {

        Assertions.assertThrowsExactly(NullPointerException.class, () -> COpt.of(null));

    }

    /**
     * 测试 empty 与 ofNullable(null) 等价
     * 对应测试用例 1.2：empty 与 ofNullable(null) 等价
     */
    @Test
    public void empty() {

        Assertions.assertEquals(COpt.empty(), COpt.ofNullable(null));

    }

    /**
     * 测试空集合、空 Map、空字符串视为空值
     * 对应测试用例 1.3：空集合/空 Map/空字符串 → 空；非空集合/Map/非空白字符串 → 有值
     */
    @Test
    public void ofEmptyAble() {

        Assertions.assertFalse(COpt.ofEmptyAble(CList.of()).isPresent());
        Assertions.assertTrue(COpt.ofEmptyAble(CList.of(1)).isPresent());

        Assertions.assertFalse(COpt.ofEmptyAble(CMap.of()).isPresent());
        Assertions.assertTrue(COpt.ofEmptyAble(CMap.of(1, 1)).isPresent());

        Assertions.assertFalse(COpt.ofEmptyAble("").isPresent());
        Assertions.assertTrue(COpt.ofEmptyAble(" ").isPresent());

    }

    /**
     * 测试空白字符串视为空值
     * 对应测试用例 1.4：空白 {@code " "} → 空；非空白 {@code "1"} → 有值
     */
    @Test
    public void ofBlankAble() {

        Assertions.assertFalse(COpt.ofBlankAble(" ").isPresent());
        Assertions.assertTrue(COpt.ofBlankAble("1").isPresent());

    }

    /**
     * 测试值存在判断
     * 对应测试用例 2.1：非空为 true、null 为 false
     */
    @Test
    public void isPresent() {

        Assertions.assertTrue(COpt.ofNullable(1).isPresent());
        Assertions.assertFalse(COpt.ofNullable(null).isPresent());

    }

    /**
     * 测试获取值
     * 对应测试用例 3.1：非空取值正确
     */
    @Test
    public void get() {

        val result = COpt.of(7)
                .get()
                ;
        Assertions.assertEquals(7, result);

    }

    /**
     * 测试空值时返回默认值
     * 对应测试用例 3.2：空值返回默认值
     */
    @Test
    public void orElse() {

        val result = COpt.ofNullable(null)
                .orElse(33)
                ;
        Assertions.assertEquals(33, result);

    }

    /**
     * 测试空值时通过供应商获取默认值
     * 对应测试用例 3.3：空值经 supplier 取默认值；值存在时不调用 supplier（惰性）
     */
    @Test
    public void orElseGet() {

        val result = COpt.ofNullable(null)
                .orElseGet(() -> 33)
                ;
        Assertions.assertEquals(33, result);

        // 值存在时 supplier 不应执行（惰性语义）
        val called = new boolean[] {false};
        val presentResult = COpt.of(7)
                .orElseGet(() -> {
                    called[0] = true;
                    return 33;
                })
                ;
        Assertions.assertEquals(7, presentResult);
        Assertions.assertFalse(called[0]);

    }

    /**
     * 测试空值时抛出指定异常
     * 对应测试用例 3.4：空值抛指定异常
     */
    @Test
    public void orElseThrow() {

        Assertions.assertThrowsExactly(IllegalArgumentException.class,
                () -> COpt.ofNullable(null)
                        .orElseThrow(IllegalArgumentException::new));

    }

    /**
     * 测试值转换
     * 对应测试用例 4.1：值映射后取值正确
     */
    @Test
    public void map() {

        val result = COpt.of(7)
                .map(String::valueOf)
                .get()
                ;
        Assertions.assertEquals("7", result);

    }

    /**
     * 测试扁平化值转换
     * 对应测试用例 4.2：扁平化映射后取值正确
     */
    @Test
    public void flatMap() {

        val result = COpt.of(7)
                .flatMap(e -> COpt.of(String.valueOf(e)))
                .get()
                ;
        Assertions.assertEquals("7", result);

    }

}
