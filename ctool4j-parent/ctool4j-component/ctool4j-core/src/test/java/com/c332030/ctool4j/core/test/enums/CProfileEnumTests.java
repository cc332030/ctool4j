package com.c332030.ctool4j.core.test.enums;

import com.c332030.ctool4j.core.enums.CProfileEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CProfileEnumTests
 * </p>
 *
 * @since 2026/1/14
 */
public class CProfileEnumTests {

    /**
     * 测试按名称获取环境枚举（大小写不敏感）
     * 对应测试用例 1.1
     */
    @Test
    public void of() {

        Assertions.assertEquals(CProfileEnum.DEFAULT, CProfileEnum.of("default"));
        Assertions.assertEquals(CProfileEnum.DEFAULT, CProfileEnum.of("DEFAULT"));

    }

    /**
     * 测试全部环境枚举按名称获取
     * 对应测试用例 1.2
     */
    @Test
    public void ofAllEnums() {

        Assertions.assertEquals(CProfileEnum.DEFAULT, CProfileEnum.of("DEFAULT"));
        Assertions.assertEquals(CProfileEnum.LOCAL, CProfileEnum.of("LOCAL"));
        Assertions.assertEquals(CProfileEnum.DEV, CProfileEnum.of("DEV"));
        Assertions.assertEquals(CProfileEnum.TEST, CProfileEnum.of("TEST"));
        Assertions.assertEquals(CProfileEnum.UAT, CProfileEnum.of("UAT"));
        Assertions.assertEquals(CProfileEnum.PROD, CProfileEnum.of("PROD"));

    }

    /**
     * 测试未知环境名抛 IllegalArgumentException
     * 对应测试用例 1.3
     */
    @Test
    public void ofUnknownThrows() {

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CProfileEnum.of("UNKNOWN")
        );
        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CProfileEnum.of("prod1")
        );
        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CProfileEnum.of("")
        );
        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CProfileEnum.of(null)
        );

    }

    /**
     * 测试生产环境集合仅含 PROD
     * 对应测试用例 2.1
     */
    @Test
    public void prodProfiles() {

        Assertions.assertEquals(1, CProfileEnum.PROD_PROFILES.size());
        Assertions.assertTrue(CProfileEnum.PROD_PROFILES.contains(CProfileEnum.PROD));
        Assertions.assertFalse(CProfileEnum.PROD_PROFILES.contains(CProfileEnum.DEV));

    }

    /**
     * 测试描述字段
     * 对应测试用例 3.1
     */
    @Test
    public void text() {

        Assertions.assertEquals("默认", CProfileEnum.DEFAULT.getText());
        Assertions.assertEquals("生产", CProfileEnum.PROD.getText());

    }

}
