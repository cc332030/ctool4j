package com.c332030.ctool4j.definition.test.enums.business;

import com.c332030.ctool4j.definition.enums.business.CCountryCodeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CCountryCodeEnumTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CCountryCodeEnumTests {

    @Test
    public void values() {

        Assertions.assertEquals(1, CCountryCodeEnum.values().length);

    }

    @Test
    public void chn() {

        Assertions.assertEquals(Integer.valueOf(86), CCountryCodeEnum.CHN.getValue());
        Assertions.assertEquals("中国", CCountryCodeEnum.CHN.getText());
        Assertions.assertEquals("CHN", CCountryCodeEnum.CHN.name());

    }

    @Test
    public void valueOfUnknown() {

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CCountryCodeEnum.valueOf("USA")
        );

    }

}
