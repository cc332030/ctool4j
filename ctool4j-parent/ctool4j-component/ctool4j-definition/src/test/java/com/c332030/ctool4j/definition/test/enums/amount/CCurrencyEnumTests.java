package com.c332030.ctool4j.definition.test.enums.amount;

import com.c332030.ctool4j.definition.enums.amount.CCurrencyEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CCurrencyEnumTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CCurrencyEnumTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void values() {

        Assertions.assertEquals(17, CCurrencyEnum.values().length);

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void cny() {

        Assertions.assertEquals("人民币", CCurrencyEnum.CNY.getText());
        Assertions.assertEquals("CNY", CCurrencyEnum.CNY.name());

    }

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void usd() {

        Assertions.assertEquals("美元", CCurrencyEnum.USD.getText());
        Assertions.assertEquals("USD", CCurrencyEnum.USD.name());

    }

    /**
     * 对应测试用例 1.4
     */
    @Test
    public void valueOfUnknown() {

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CCurrencyEnum.valueOf("RUB1")
        );

    }

}
