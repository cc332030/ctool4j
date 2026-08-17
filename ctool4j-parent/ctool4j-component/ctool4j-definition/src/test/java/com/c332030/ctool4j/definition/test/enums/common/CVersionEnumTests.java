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

    @Test
    public void values() {

        Assertions.assertEquals(3, CVersionEnum.values().length);

    }

    @Test
    public void v1() {

        Assertions.assertEquals("V1", CVersionEnum.V1.getText());
        Assertions.assertEquals("V1", CVersionEnum.V1.name());

    }

    @Test
    public void v2() {

        Assertions.assertEquals("V2", CVersionEnum.V2.getText());
        Assertions.assertEquals("V2", CVersionEnum.V2.name());

    }

    @Test
    public void v3() {

        Assertions.assertEquals("V3", CVersionEnum.V3.getText());
        Assertions.assertEquals("V3", CVersionEnum.V3.name());

    }

    @Test
    public void valueOf_normal() {

        Assertions.assertSame(CVersionEnum.V1, CVersionEnum.valueOf("V1"));
        Assertions.assertSame(CVersionEnum.V2, CVersionEnum.valueOf("V2"));
        Assertions.assertSame(CVersionEnum.V3, CVersionEnum.valueOf("V3"));

    }

    @Test
    public void valueOfUnknown() {

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CVersionEnum.valueOf("UNKNOWN")
        );

    }

}
