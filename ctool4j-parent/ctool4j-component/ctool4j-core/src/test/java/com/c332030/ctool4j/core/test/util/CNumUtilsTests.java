package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CNumUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/**
 * <p>
 * Description: CNumUtilsTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「溢出校验 / 转 int / 转 62 进制 / 最值 / 百分比」多个维度组织。</li>
 *   <li>溢出校验覆盖 int 边界（MIN/MAX 不抛）、超 int 边界抛、long 极值抛，以及 double→float 溢出。</li>
 *   <li>转 int 覆盖正常值、溢出返回 null。</li>
 *   <li>转 62 进制覆盖各类型值、进位边界（62/63）、负数抛异常。</li>
 *   <li>最值覆盖正负混合、单/多值。</li>
 *   <li>百分比覆盖三种入参（Integer/Long/BigDecimal）、scale、total 为 0 返回 null。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对溢出抛异常、toInt 溢出返回 null、to62 负数抛异常、percent total=0 返回 null 的约定。</li>
 *   <li>依据测试方法（等价类/边界值/异常路径）：int/float 边界、62 进位、负数、total=0。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：assertOverflow(long) int 边界与溢出；assertOverflow(double) float 边界与溢出；toInt 正常与溢出；</li>
 *   <li>to62 各类型与边界（0/62/63）与负数异常；max/min 正负混合；percent 三种入参、scale、total=0 返回 null。</li>
 *   <li>未覆盖：{@code defaultZero}/{@code greaterThanZero}/{@code lessThanZero}/{@code sum}/{@code divide}/{@code scale}/{@code compare}/</li>
 *   <li>{@code parseInt}/{@code parseLong}/{@code toStringThenParseInt} 等其余入口（当前测试聚焦溢出/转换/最值/百分比核心路径，</li>
 *   <li>其余入口行为可后续批次补充）。</li>
 * </ul>
 * <h2>溢出校验</h2>
 * <ul>
 *   <li>1.1 long 校验：int MIN/MAX 不抛；超 int 边界抛 ArithmeticException；long 极值抛（assertOverflowLong）</li>
 *   <li>1.2 double 校验：float 极值不抛；Double.MAX_VALUE 抛 ArithmeticException；Double.MIN_VALUE 不抛（assertOverflowDouble）</li>
 * </ul>
 * <h2>数值转换</h2>
 * <ul>
 *   <li>2.1 toInt：正常值返回；溢出（超出 int）返回 null（toInt）</li>
 *   <li>2.2 to62：各类型值正确；0→"0"；62→"10"、63→"11"进位；负数抛 IllegalArgumentException（to62）</li>
 * </ul>
 * <h2>最值</h2>
 * <ul>
 *   <li>3.1 max：正负混合取最大（max）</li>
 *   <li>3.2 min：正负混合取最小（min）</li>
 * </ul>
 * <h2>百分比</h2>
 * <ul>
 *   <li>4.1 percent：Integer/Long/BigDecimal 三种入参；默认 scale 与显式 scale（percent）</li>
 *   <li>4.2 percent total=0：三种入参均返回 null（percentTotalZero）</li>
 * </ul>
 *
 * @since 2025/12/22
 * @version 1.0
 */
public class CNumUtilsTests {

    /**
     * 测试 long 溢出校验
     * 对应测试用例 1.1：long 校验：int MIN/MAX 不抛；超 int 边界抛 ArithmeticException；long 极值抛
     */
    @Test
    public void assertOverflowLong() {

        CNumUtils.assertOverflow(Integer.MIN_VALUE);
        CNumUtils.assertOverflow(Integer.MAX_VALUE);

        Assertions.assertThrowsExactly(ArithmeticException.class, () -> CNumUtils.assertOverflow(Integer.MIN_VALUE - 1L));
        Assertions.assertThrowsExactly(ArithmeticException.class, () -> CNumUtils.assertOverflow(Integer.MAX_VALUE + 1L));

        Assertions.assertThrowsExactly(ArithmeticException.class, () -> CNumUtils.assertOverflow(Long.MIN_VALUE));
        Assertions.assertThrowsExactly(ArithmeticException.class, () -> CNumUtils.assertOverflow(Long.MAX_VALUE));

    }

    /**
     * 测试 double 溢出校验
     * 对应测试用例 1.2：double 校验：float 极值不抛；Double.MAX_VALUE 抛 ArithmeticException；Double.MIN_VALUE 不抛
     */
    @Test
    public void assertOverflowDouble() {

        CNumUtils.assertOverflow(Float.MIN_VALUE);
        CNumUtils.assertOverflow(Float.MAX_VALUE);

        // Double.MIN_VALUE 极小正数，未超出 Float 范围，不抛异常
        Assertions.assertDoesNotThrow(() -> CNumUtils.assertOverflow(Double.MIN_VALUE));
        Assertions.assertThrowsExactly(ArithmeticException.class, () -> CNumUtils.assertOverflow(Double.MAX_VALUE));

    }

    /**
     * 测试数值转 int（溢出时返回 null）
     * 对应测试用例 2.1：正常值返回；溢出（超出 int）返回 null
     */
    @Test
    public void toInt() {

        Assertions.assertEquals(1, CNumUtils.toInt(1));
        Assertions.assertEquals(Integer.MIN_VALUE, CNumUtils.toInt(Integer.MIN_VALUE));
        Assertions.assertEquals(Integer.MAX_VALUE, CNumUtils.toInt(Integer.MAX_VALUE));

        Assertions.assertNull(CNumUtils.toInt((long) Integer.MAX_VALUE + 1));
        Assertions.assertNull(CNumUtils.toInt(Long.valueOf(Integer.MAX_VALUE + 1L)));

        Assertions.assertNull(CNumUtils.toInt(Long.MIN_VALUE));
        Assertions.assertNull(CNumUtils.toInt(Long.MAX_VALUE));

    }

    /**
     * 测试数值转 Base62 字符串
     * 对应测试用例 2.2：各类型值正确；0→"0"；62→"10"、63→"11"进位；负数抛 IllegalArgumentException
     */
    @Test
    public void to62() {

        Assertions.assertEquals("1", CNumUtils.to62(1));
        Assertions.assertEquals("10", CNumUtils.to62(62));
        Assertions.assertEquals("100", CNumUtils.to62(3844));

        Assertions.assertEquals("23", CNumUtils.to62(Byte.MAX_VALUE));
        Assertions.assertEquals("8wv", CNumUtils.to62(Short.MAX_VALUE));
        Assertions.assertEquals("2lkCB1", CNumUtils.to62(Integer.MAX_VALUE));
        Assertions.assertEquals("aZl8N0y58M7", CNumUtils.to62(Long.MAX_VALUE));

        // 边界：0 的 62 进制为 "0"
        Assertions.assertEquals("0", CNumUtils.to62(0));

        // 边界：62 与 63 的进位
        Assertions.assertEquals("11", CNumUtils.to62(63));

        // 反例：负数不支持，快速失败
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> CNumUtils.to62(-1));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> CNumUtils.to62(Long.MIN_VALUE));

    }

    /**
     * 测试取最大值
     * 对应测试用例 3.1：正负混合取最大
     */
    @Test
    public void max() {

        Assertions.assertEquals(9, CNumUtils.max(
            -1, 1, 3, 9
        ));

        Assertions.assertEquals(9, CNumUtils.max(
            1, 3, 9
        ));

        Assertions.assertEquals(-1, CNumUtils.max(
            -1, -3, -9
        ));

    }

    /**
     * 测试取最小值
     * 对应测试用例 3.2：正负混合取最小
     */
    @Test
    public void min() {

        Assertions.assertEquals(-1, CNumUtils.min(
            -1, 1, 3, 9
        ));

        Assertions.assertEquals(1, CNumUtils.min(
            1, 3, 9
        ));

        Assertions.assertEquals(-9, CNumUtils.min(
            -1, -3, -9
        ));

    }

    /**
     * 测试百分比计算
     * 对应测试用例 4.1：Integer/Long/BigDecimal 三种入参；默认 scale 与显式 scale
     */
    @Test
    public void percent() {

        Assertions.assertEquals("25", CNumUtils.percent(1, 4).toString());
        Assertions.assertEquals("25", CNumUtils.percent(1L, 4L).toString());
        Assertions.assertEquals("25", CNumUtils.percent(new BigDecimal(1), new BigDecimal(4)).toString());

        Assertions.assertEquals("18.75", CNumUtils.percent(3, 16, 2).toString());
        Assertions.assertEquals("18.75", CNumUtils.percent(3L, 16L, 2).toString());
        Assertions.assertEquals("18.75", CNumUtils.percent(new BigDecimal(3), new BigDecimal(16), 2).toString());

    }

    /**
     * 测试百分比计算：total 为 0 时返回 null
     * 对应测试用例 4.2：percent total=0：三种入参均返回 null
     */
    @Test
    public void percentTotalZero() {

        Assertions.assertNull(CNumUtils.percent(1, 0));
        Assertions.assertNull(CNumUtils.percent(1L, 0L));
        Assertions.assertNull(CNumUtils.percent(new BigDecimal(1), BigDecimal.ZERO));

        Assertions.assertNull(CNumUtils.percent(1, 0, 2));
        Assertions.assertNull(CNumUtils.percent(1L, 0L, 2));
        Assertions.assertNull(CNumUtils.percent(new BigDecimal(1), BigDecimal.ZERO, 2));

    }

}
