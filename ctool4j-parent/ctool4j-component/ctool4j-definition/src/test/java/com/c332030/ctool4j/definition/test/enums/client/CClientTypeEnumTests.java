package com.c332030.ctool4j.definition.test.enums.client;

import com.c332030.ctool4j.definition.enums.client.CClientTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CClientTypeEnumTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证枚举数量、各枚举描述与名称、valueOf 正常与未知名抛异常。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对枚举元素与描述的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：枚举数量；WEB/公众号/小程序/APP/PC 描述；valueOf 正常；未知名抛异常。</li>
 *   <li>未覆盖：无。</li>
 * </ul>
 * <h2>枚举</h2>
 * <ul>
 *   <li>1.1 values：5 个枚举（values）</li>
 *   <li>1.2 web：{@code 网页}/WEB（web）</li>
 *   <li>1.3 officialAccount：{@code 公众号}（officialAccount）</li>
 *   <li>1.4 miniProgram：{@code 小程序}（miniProgram）</li>
 *   <li>1.5 app：{@code 应用}/APP（app）</li>
 *   <li>1.6 pc：{@code PC}/PC（pc）</li>
 *   <li>1.7 valueOf 正常（valueOf_normal）</li>
 *   <li>1.8 未知名抛异常（valueOfUnknown）</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.0
 */
public class CClientTypeEnumTests {

    /**
     * 对应测试用例 1.1：5 个枚举
     */
    @Test
    public void values() {

        Assertions.assertEquals(5, CClientTypeEnum.values().length);

    }

    /**
     * 对应测试用例 1.2：{@code 网页}/WEB
     */
    @Test
    public void web() {

        Assertions.assertEquals("网页", CClientTypeEnum.WEB.getText());
        Assertions.assertEquals("WEB", CClientTypeEnum.WEB.name());

    }

    /**
     * 对应测试用例 1.3：{@code 公众号}
     */
    @Test
    public void officialAccount() {

        Assertions.assertEquals("公众号", CClientTypeEnum.OFFICIAL_ACCOUNT.getText());
        Assertions.assertEquals("OFFICIAL_ACCOUNT", CClientTypeEnum.OFFICIAL_ACCOUNT.name());

    }

    /**
     * 对应测试用例 1.4：{@code 小程序}
     */
    @Test
    public void miniProgram() {

        Assertions.assertEquals("小程序", CClientTypeEnum.MINI_PROGRAM.getText());
        Assertions.assertEquals("MINI_PROGRAM", CClientTypeEnum.MINI_PROGRAM.name());

    }

    /**
     * 对应测试用例 1.5：{@code 应用}/APP
     */
    @Test
    public void app() {

        Assertions.assertEquals("应用", CClientTypeEnum.APP.getText());
        Assertions.assertEquals("APP", CClientTypeEnum.APP.name());

    }

    /**
     * 对应测试用例 1.6：{@code PC}/PC
     */
    @Test
    public void pc() {

        Assertions.assertEquals("PC", CClientTypeEnum.PC.getText());
        Assertions.assertEquals("PC", CClientTypeEnum.PC.name());

    }

    /**
     * 对应测试用例 1.7：valueOf 正常
     */
    @Test
    public void valueOf_normal() {

        Assertions.assertSame(CClientTypeEnum.WEB, CClientTypeEnum.valueOf("WEB"));
        Assertions.assertSame(CClientTypeEnum.OFFICIAL_ACCOUNT, CClientTypeEnum.valueOf("OFFICIAL_ACCOUNT"));
        Assertions.assertSame(CClientTypeEnum.MINI_PROGRAM, CClientTypeEnum.valueOf("MINI_PROGRAM"));
        Assertions.assertSame(CClientTypeEnum.APP, CClientTypeEnum.valueOf("APP"));
        Assertions.assertSame(CClientTypeEnum.PC, CClientTypeEnum.valueOf("PC"));

    }

    /**
     * 对应测试用例 1.8：未知名抛异常
     */
    @Test
    public void valueOfUnknown() {

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CClientTypeEnum.valueOf("UNKNOWN")
        );

    }

}
