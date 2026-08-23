package com.c332030.ctool4j.definition.test.enums.common;

import com.c332030.ctool4j.definition.enums.common.CVersionEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CVersionEnumTests
 * </p>
 *
 * @since 2026/8/16
 */
public class CVersionEnumTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void values() {

        Assertions.assertEquals(3, CVersionEnum.values().length);

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void v1() {

        Assertions.assertEquals("V1", CVersionEnum.V1.getText());
        Assertions.assertEquals("V1", CVersionEnum.V1.name());

    }

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void v2() {

        Assertions.assertEquals("V2", CVersionEnum.V2.getText());
        Assertions.assertEquals("V2", CVersionEnum.V2.name());

    }

    /**
     * 对应测试用例 1.4
     */
    @Test
    public void v3() {

        Assertions.assertEquals("V3", CVersionEnum.V3.getText());
        Assertions.assertEquals("V3", CVersionEnum.V3.name());

    }

    /**
     * 对应测试用例 1.5
     */
    @Test
    public void valueOf_normal() {

        Assertions.assertSame(CVersionEnum.V1, CVersionEnum.valueOf("V1"));
        Assertions.assertSame(CVersionEnum.V2, CVersionEnum.valueOf("V2"));
        Assertions.assertSame(CVersionEnum.V3, CVersionEnum.valueOf("V3"));

    }

    /**
     * 对应测试用例 1.6
     */
    @Test
    public void valueOfUnknown() {

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CVersionEnum.valueOf("UNKNOWN")
        );

    }

}
