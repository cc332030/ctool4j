package com.c332030.ctool4j.definition.test.enums.client;

import com.c332030.ctool4j.definition.enums.client.CClientTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CClientTypeEnumTests
 * </p>
 *
 * @since 2026/8/16
 */
public class CClientTypeEnumTests {

    @Test
    public void values() {

        Assertions.assertEquals(5, CClientTypeEnum.values().length);

    }

    @Test
    public void web() {

        Assertions.assertEquals("网页", CClientTypeEnum.WEB.getText());
        Assertions.assertEquals("WEB", CClientTypeEnum.WEB.name());

    }

    @Test
    public void officialAccount() {

        Assertions.assertEquals("公众号", CClientTypeEnum.OFFICIAL_ACCOUNT.getText());
        Assertions.assertEquals("OFFICIAL_ACCOUNT", CClientTypeEnum.OFFICIAL_ACCOUNT.name());

    }

    @Test
    public void miniProgram() {

        Assertions.assertEquals("小程序", CClientTypeEnum.MINI_PROGRAM.getText());
        Assertions.assertEquals("MINI_PROGRAM", CClientTypeEnum.MINI_PROGRAM.name());

    }

    @Test
    public void app() {

        Assertions.assertEquals("应用", CClientTypeEnum.APP.getText());
        Assertions.assertEquals("APP", CClientTypeEnum.APP.name());

    }

    @Test
    public void pc() {

        Assertions.assertEquals("PC", CClientTypeEnum.PC.getText());
        Assertions.assertEquals("PC", CClientTypeEnum.PC.name());

    }

    @Test
    public void valueOf_normal() {

        Assertions.assertSame(CClientTypeEnum.WEB, CClientTypeEnum.valueOf("WEB"));
        Assertions.assertSame(CClientTypeEnum.OFFICIAL_ACCOUNT, CClientTypeEnum.valueOf("OFFICIAL_ACCOUNT"));
        Assertions.assertSame(CClientTypeEnum.MINI_PROGRAM, CClientTypeEnum.valueOf("MINI_PROGRAM"));
        Assertions.assertSame(CClientTypeEnum.APP, CClientTypeEnum.valueOf("APP"));
        Assertions.assertSame(CClientTypeEnum.PC, CClientTypeEnum.valueOf("PC"));

    }

    @Test
    public void valueOfUnknown() {

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CClientTypeEnum.valueOf("UNKNOWN")
        );

    }

}
