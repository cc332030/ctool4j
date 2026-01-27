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

    @Test
    public void assertOverflowLong() {

        CNumUtils.assertOverflow(Integer.MIN_VALUE);
        CNumUtils.assertOverflow(Integer.MAX_VALUE);

        Assertions.assertThrowsExactly(ArithmeticException.class, () -> CNumUtils.assertOverflow(Integer.MIN_VALUE - 1L));
        Assertions.assertThrowsExactly(ArithmeticException.class, () -> CNumUtils.assertOverflow(Integer.MAX_VALUE + 1L));

        Assertions.assertThrowsExactly(ArithmeticException.class, () -> CNumUtils.assertOverflow(Long.MIN_VALUE));
        Assertions.assertThrowsExactly(ArithmeticException.class, () -> CNumUtils.assertOverflow(Long.MAX_VALUE));

    }

    @Test
    public void assertOverflowDouble() {

        CNumUtils.assertOverflow(Float.MIN_VALUE);
        CNumUtils.assertOverflow(Float.MAX_VALUE);

        Assertions.assertThrowsExactly(ArithmeticException.class, () -> CNumUtils.assertOverflow(Double.MIN_VALUE));
        Assertions.assertThrowsExactly(ArithmeticException.class, () -> CNumUtils.assertOverflow(Double.MAX_VALUE));

    }

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

    @Test
    public void to62() {

        Assertions.assertEquals("1", CNumUtils.to62(1));
        Assertions.assertEquals("10", CNumUtils.to62(62));
        Assertions.assertEquals("100", CNumUtils.to62(3844));

        Assertions.assertEquals("23", CNumUtils.to62(Byte.MAX_VALUE));
        Assertions.assertEquals("8wv", CNumUtils.to62(Short.MAX_VALUE));
        Assertions.assertEquals("2lkCB1", CNumUtils.to62(Integer.MAX_VALUE));
        Assertions.assertEquals("aZl8N0y58M7", CNumUtils.to62(Long.MAX_VALUE));

    }

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

    @Test
    public void percent() {

        Assertions.assertEquals("25", CNumUtils.percent(1, 4).toString());
        Assertions.assertEquals("25", CNumUtils.percent(1L, 4L).toString());
        Assertions.assertEquals("25", CNumUtils.percent(new BigDecimal(1), new BigDecimal(4)).toString());

        Assertions.assertEquals("18.75", CNumUtils.percent(3, 16, 2).toString());
        Assertions.assertEquals("18.75", CNumUtils.percent(3L, 16L, 2).toString());
        Assertions.assertEquals("18.75", CNumUtils.percent(new BigDecimal(3), new BigDecimal(16), 2).toString());

    }

}
