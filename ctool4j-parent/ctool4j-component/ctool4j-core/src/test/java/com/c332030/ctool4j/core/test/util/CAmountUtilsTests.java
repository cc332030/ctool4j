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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「入参形态」分三类（Integer / Long / BigDecimal），每类覆盖正例、边界（0、负数）与 null。</li>
 *   <li>正例覆盖典型值与小数结果：整数分（123 → 1.23）、长整型分（123456 → 1234.56）、</li>
 *   <li>BigDecimal 分含小数（123.456 → 1.23，验证四舍五入）。</li>
 *   <li>边界覆盖 0（0 分 → 0.00）与负数（-123 → -1.23），null 统一断言返回 null。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对换算规则的约定（元 = 分 ÷ 100，四舍五入保留 2 位小数）。</li>
 *   <li>依据功能设计对 null 返回 null 的约定。</li>
 *   <li>依据测试方法（等价类/边界值）：典型值、0 边界、负数反例、null 异常输入、BigDecimal 小数四舍五入分支。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：三种入参形态的正例、0、负数、null；BigDecimal 小数四舍五入。</li>
 *   <li>未覆盖：元 → 分反向换算（本类不提供）；超 2 位小数的元金额精确运算（会四舍五入，见设计文档边界）。</li>
 * </ul>
 * <h2>toYuan(Integer)</h2>
 * <ul>
 *   <li>1.1 正例：123 → 1.23、1 → 0.01（toYuanByInteger）</li>
 *   <li>1.2 边界：0 → 0.00（toYuanByInteger）</li>
 *   <li>1.3 反例：-123 → -1.23（toYuanByInteger）</li>
 *   <li>1.4 边界：null → null（toYuanByInteger）</li>
 * </ul>
 * <h2>toYuan(Long)</h2>
 * <ul>
 *   <li>2.1 正例：123456L → 1234.56（toYuanByLong）</li>
 *   <li>2.2 边界：0L → 0.00（toYuanByLong）</li>
 *   <li>2.3 边界：null → null（toYuanByLong）</li>
 * </ul>
 * <h2>toYuan(BigDecimal)</h2>
 * <ul>
 *   <li>3.1 正例：123 → 1.23（toYuanByBigDecimal）</li>
 *   <li>3.2 边界/分支：123.456 → 1.23，验证四舍五入保留 2 位小数（toYuanByBigDecimal）</li>
 *   <li>3.3 边界：null → null（toYuanByBigDecimal）</li>
 * </ul>
 *
 * @since 2025/12/18
 * @version 1.0
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
