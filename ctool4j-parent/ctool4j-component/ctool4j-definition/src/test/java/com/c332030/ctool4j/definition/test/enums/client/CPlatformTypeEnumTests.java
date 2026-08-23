package com.c332030.ctool4j.definition.test.enums.client;

import com.c332030.ctool4j.definition.enums.client.CPlatformTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CPlatformTypeEnumTests
 * </p>
 *
 * @since 2026/8/16
 */
public class CPlatformTypeEnumTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void values() {

        Assertions.assertEquals(8, CPlatformTypeEnum.values().length);

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void web() {

        Assertions.assertEquals("网页", CPlatformTypeEnum.WEB.getText());
        Assertions.assertEquals("WEB", CPlatformTypeEnum.WEB.name());

    }

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void android() {

        Assertions.assertEquals("安卓", CPlatformTypeEnum.ANDROID.getText());
        Assertions.assertEquals("ANDROID", CPlatformTypeEnum.ANDROID.name());

    }

    /**
     * 对应测试用例 1.4
     */
    @Test
    public void ios() {

        Assertions.assertEquals("iOS", CPlatformTypeEnum.IOS.getText());
        Assertions.assertEquals("IOS", CPlatformTypeEnum.IOS.name());

    }

    /**
     * 对应测试用例 1.5
     */
    @Test
    public void harmonyOs() {

        Assertions.assertEquals("HarmonyOS", CPlatformTypeEnum.HARMONY_OS.getText());
        Assertions.assertEquals("HARMONY_OS", CPlatformTypeEnum.HARMONY_OS.name());

    }

    /**
     * 对应测试用例 1.6
     */
    @Test
    public void windows() {

        Assertions.assertEquals("Windows", CPlatformTypeEnum.WINDOWS.getText());
        Assertions.assertEquals("WINDOWS", CPlatformTypeEnum.WINDOWS.name());

    }

    /**
     * 对应测试用例 1.7
     */
    @Test
    public void macOs() {

        Assertions.assertEquals("MacOS", CPlatformTypeEnum.MAC_OS.getText());
        Assertions.assertEquals("MAC_OS", CPlatformTypeEnum.MAC_OS.name());

    }

    /**
     * 对应测试用例 1.8
     */
    @Test
    public void linux() {

        Assertions.assertEquals("Linux", CPlatformTypeEnum.LINUX.getText());
        Assertions.assertEquals("LINUX", CPlatformTypeEnum.LINUX.name());

    }

    /**
     * 对应测试用例 1.9
     */
    @Test
    public void wearable() {

        Assertions.assertEquals("Wearable", CPlatformTypeEnum.WEARABLE.getText());
        Assertions.assertEquals("WEARABLE", CPlatformTypeEnum.WEARABLE.name());

    }

    /**
     * 对应测试用例 1.10
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
     * 对应测试用例 1.11
     */
    @Test
    public void valueOfUnknown() {

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CPlatformTypeEnum.valueOf("UNKNOWN")
        );

    }

}
