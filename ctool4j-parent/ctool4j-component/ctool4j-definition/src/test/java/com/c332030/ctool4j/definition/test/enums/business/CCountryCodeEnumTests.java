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

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void values() {

        Assertions.assertEquals(1, CCountryCodeEnum.values().length);

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void chn() {

        Assertions.assertEquals(Integer.valueOf(86), CCountryCodeEnum.CHN.getValue());
        Assertions.assertEquals("中国", CCountryCodeEnum.CHN.getText());
        Assertions.assertEquals("CHN", CCountryCodeEnum.CHN.name());

    }

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void valueOfUnknown() {

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CCountryCodeEnum.valueOf("USA")
        );

    }

}
