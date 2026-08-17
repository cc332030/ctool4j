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
 * @since 2025/12/22
 */
public class CNumUtilsTests {

    /**
     * 测试 long 溢出校验
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
