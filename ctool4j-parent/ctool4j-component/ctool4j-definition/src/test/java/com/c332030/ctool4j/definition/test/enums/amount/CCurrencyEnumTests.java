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

    @Test
    public void values() {

        Assertions.assertEquals(17, CCurrencyEnum.values().length);

    }

    @Test
    public void cny() {

        Assertions.assertEquals("人民币", CCurrencyEnum.CNY.getText());
        Assertions.assertEquals("CNY", CCurrencyEnum.CNY.name());

    }

    @Test
    public void usd() {

        Assertions.assertEquals("美元", CCurrencyEnum.USD.getText());
        Assertions.assertEquals("USD", CCurrencyEnum.USD.name());

    }

    @Test
    public void valueOfUnknown() {

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CCurrencyEnum.valueOf("RUB1")
        );

    }

}
