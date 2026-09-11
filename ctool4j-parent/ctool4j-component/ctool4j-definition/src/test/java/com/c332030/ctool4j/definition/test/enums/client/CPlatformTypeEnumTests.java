package com.c332030.ctool4j.definition.test.enums.client;

import com.c332030.ctool4j.definition.enums.client.CPlatformTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CPlatformTypeEnumTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证枚举数量、各枚举描述、valueOf 正常与未知名抛异常。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对枚举元素与描述的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：枚举数量；8 个平台描述；valueOf 正常；未知名抛异常。</li>
 *   <li>未覆盖：无。</li>
 * </ul>
 * <h2>枚举</h2>
 * <ul>
 *   <li>1.1 values：8 个枚举（values）</li>
 *   <li>1.2 web：{@code 网页}（web）</li>
 *   <li>1.3 android：{@code 安卓}（android）</li>
 *   <li>1.4 ios：{@code iOS}（ios）</li>
 *   <li>1.5 harmonyOs：{@code HarmonyOS}（harmonyOs）</li>
 *   <li>1.6 windows：{@code Windows}（windows）</li>
 *   <li>1.7 macOs：{@code MacOS}（macOs）</li>
 *   <li>1.8 linux：{@code Linux}（linux）</li>
 *   <li>1.9 wearable：{@code Wearable}（wearable）</li>
 *   <li>1.10 valueOf 正常（valueOf_normal）</li>
 *   <li>1.11 未知名抛异常（valueOfUnknown）</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.0
 */
public class CPlatformTypeEnumTests {

    /**
     * 对应测试用例 1.1：8 个枚举
     */
    @Test
    public void values() {

        Assertions.assertEquals(8, CPlatformTypeEnum.values().length);

    }

    /**
     * 对应测试用例 1.2：{@code 网页}
     */
    @Test
    public void web() {

        Assertions.assertEquals("网页", CPlatformTypeEnum.WEB.getText());
        Assertions.assertEquals("WEB", CPlatformTypeEnum.WEB.name());

    }

    /**
     * 对应测试用例 1.3：{@code 安卓}
     */
    @Test
    public void android() {

        Assertions.assertEquals("安卓", CPlatformTypeEnum.ANDROID.getText());
        Assertions.assertEquals("ANDROID", CPlatformTypeEnum.ANDROID.name());

    }

    /**
     * 对应测试用例 1.4：{@code iOS}
     */
    @Test
    public void ios() {

        Assertions.assertEquals("iOS", CPlatformTypeEnum.IOS.getText());
        Assertions.assertEquals("IOS", CPlatformTypeEnum.IOS.name());

    }

    /**
     * 对应测试用例 1.5：{@code HarmonyOS}
     */
    @Test
    public void harmonyOs() {

        Assertions.assertEquals("HarmonyOS", CPlatformTypeEnum.HARMONY_OS.getText());
        Assertions.assertEquals("HARMONY_OS", CPlatformTypeEnum.HARMONY_OS.name());

    }

    /**
     * 对应测试用例 1.6：{@code Windows}
     */
    @Test
    public void windows() {

        Assertions.assertEquals("Windows", CPlatformTypeEnum.WINDOWS.getText());
        Assertions.assertEquals("WINDOWS", CPlatformTypeEnum.WINDOWS.name());

    }

    /**
     * 对应测试用例 1.7：{@code MacOS}
     */
    @Test
    public void macOs() {

        Assertions.assertEquals("MacOS", CPlatformTypeEnum.MAC_OS.getText());
        Assertions.assertEquals("MAC_OS", CPlatformTypeEnum.MAC_OS.name());

    }

    /**
     * 对应测试用例 1.8：{@code Linux}
     */
    @Test
    public void linux() {

        Assertions.assertEquals("Linux", CPlatformTypeEnum.LINUX.getText());
        Assertions.assertEquals("LINUX", CPlatformTypeEnum.LINUX.name());

    }

    /**
     * 对应测试用例 1.9：{@code Wearable}
     */
    @Test
    public void wearable() {

        Assertions.assertEquals("Wearable", CPlatformTypeEnum.WEARABLE.getText());
        Assertions.assertEquals("WEARABLE", CPlatformTypeEnum.WEARABLE.name());

    }

    /**
     * 对应测试用例 1.10：valueOf 正常
     */
    @Test
    public void valueOf_normal() {

        Assertions.assertSame(CPlatformTypeEnum.WEB, CPlatformTypeEnum.valueOf("WEB"));
        Assertions.assertSame(CPlatformTypeEnum.ANDROID, CPlatformTypeEnum.valueOf("ANDROID"));
        Assertions.assertSame(CPlatformTypeEnum.IOS, CPlatformTypeEnum.valueOf("IOS"));
        Assertions.assertSame(CPlatformTypeEnum.HARMONY_OS, CPlatformTypeEnum.valueOf("HARMONY_OS"));
        Assertions.assertSame(CPlatformTypeEnum.WINDOWS, CPlatformTypeEnum.valueOf("WINDOWS"));
        Assertions.assertSame(CPlatformTypeEnum.MAC_OS, CPlatformTypeEnum.valueOf("MAC_OS"));
        Assertions.assertSame(CPlatformTypeEnum.LINUX, CPlatformTypeEnum.valueOf("LINUX"));
        Assertions.assertSame(CPlatformTypeEnum.WEARABLE, CPlatformTypeEnum.valueOf("WEARABLE"));

    }

    /**
     * 对应测试用例 1.11：未知名抛异常
     */
    @Test
    public void valueOfUnknown() {

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CPlatformTypeEnum.valueOf("UNKNOWN")
        );

    }

}
