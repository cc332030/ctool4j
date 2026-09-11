package com.c332030.ctool4j.definition.test.enums.amount;

import com.c332030.ctool4j.definition.enums.amount.CCurrencyEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CCurrencyEnumTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证枚举数量、CNY/USD 描述与名称，以及未知名抛异常。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对枚举元素与描述的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：枚举数量；CNY/USD 描述与名称；未知名抛异常。</li>
 *   <li>未覆盖：其余货币（CNY/USD 代表样本，其余结构一致）。</li>
 * </ul>
 * <h2>枚举</h2>
 * <ul>
 *   <li>1.1 values：17 个枚举（values）</li>
 *   <li>1.2 cny：{@code 人民币}/CNY（cny）</li>
 *   <li>1.3 usd：{@code 美元}/USD（usd）</li>
 *   <li>1.4 未知名抛异常（valueOfUnknown）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CCurrencyEnumTests {

    /**
     * 对应测试用例 1.1：17 个枚举
     */
    @Test
    public void values() {

        Assertions.assertEquals(17, CCurrencyEnum.values().length);

    }

    /**
     * 对应测试用例 1.2：{@code 人民币}/CNY
     */
    @Test
    public void cny() {

        Assertions.assertEquals("人民币", CCurrencyEnum.CNY.getText());
        Assertions.assertEquals("CNY", CCurrencyEnum.CNY.name());

    }

    /**
     * 对应测试用例 1.3：{@code 美元}/USD
     */
    @Test
    public void usd() {

        Assertions.assertEquals("美元", CCurrencyEnum.USD.getText());
        Assertions.assertEquals("USD", CCurrencyEnum.USD.name());

    }

    /**
     * 对应测试用例 1.4：未知名抛异常
     */
    @Test
    public void valueOfUnknown() {

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CCurrencyEnum.valueOf("RUB1")
        );

    }

}
