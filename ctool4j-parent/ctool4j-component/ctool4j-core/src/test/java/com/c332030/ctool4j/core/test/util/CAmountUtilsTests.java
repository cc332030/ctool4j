package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CAmountUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/**
 * <p>
 * Description: CAmountUtilsTests
 * </p>
 *
 * @since 2025/12/18
 */
public class CAmountUtilsTests {

    /**
     * 对应测试用例 1.1 / 1.2 / 1.3 / 1.4
     */
    @Test
    public void toYuanByInteger() {

        Assertions.assertEquals(new BigDecimal("1.23"), CAmountUtils.toYuan(123));
        Assertions.assertEquals(new BigDecimal("0.01"), CAmountUtils.toYuan(1));
        Assertions.assertEquals(new BigDecimal("0.00"), CAmountUtils.toYuan(0));
        Assertions.assertEquals(new BigDecimal("-1.23"), CAmountUtils.toYuan(-123));
        Assertions.assertNull(CAmountUtils.toYuan((Integer) null));

    }

    /**
     * 对应测试用例 2.1 / 2.2 / 2.3
     */
    @Test
    public void toYuanByLong() {

        Assertions.assertEquals(new BigDecimal("1234.56"), CAmountUtils.toYuan(123456L));
        Assertions.assertEquals(new BigDecimal("0.00"), CAmountUtils.toYuan(0L));
        Assertions.assertNull(CAmountUtils.toYuan((Long) null));

    }

    /**
     * 对应测试用例 3.1 / 3.2 / 3.3
     */
    @Test
    public void toYuanByBigDecimal() {

        Assertions.assertEquals(new BigDecimal("1.23"), CAmountUtils.toYuan(new BigDecimal(123)));
        Assertions.assertEquals(new BigDecimal("1.23"), CAmountUtils.toYuan(new BigDecimal("123.456")));
        Assertions.assertNull(CAmountUtils.toYuan((BigDecimal) null));

    }

}
