package com.c332030.ctool4j.core.test.enums;

import com.c332030.ctool4j.core.enums.CProfileEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CProfileEnumTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「按名获取 / 全枚举 / 未知异常 / 生产集合 / 描述」多个维度组织。</li>
 *   <li>按名获取覆盖大小写不敏感；全枚举覆盖每个环境；未知异常覆盖未知名/近似名/空/null；</li>
 *   <li>生产集合验证仅含 PROD；描述覆盖默认与生产。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对忽略大小写反查、未知抛异常、PROD_PROFILES 仅含 PROD 的约定。</li>
 *   <li>依据测试方法（等价类/边界值/异常路径）：大小写、全枚举、未知/空/null。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：of 大小写不敏感；6 个环境按名获取；未知/近似/空/null 抛异常；PROD_PROFILES 含 PROD 不含 DEV；</li>
 *   <li>描述。</li>
 *   <li>未覆盖：无（覆盖了全部入口与边界）。</li>
 * </ul>
 * <h2>按名获取（of）</h2>
 * <ul>
 *   <li>1.1 大小写不敏感：{@code default}/{@code DEFAULT} 均返回 DEFAULT（of）</li>
 *   <li>1.2 全枚举：6 个环境按名获取（ofAllEnums）</li>
 *   <li>1.3 未知异常：{@code UNKNOWN}/{@code prod1}/空/null 抛 IllegalArgumentException（ofUnknownThrows）</li>
 * </ul>
 * <h2>生产环境集合</h2>
 * <ul>
 *   <li>2.1 PROD_PROFILES：大小 1 仅含 PROD、不含 DEV（prodProfiles）</li>
 * </ul>
 * <h2>描述</h2>
 * <ul>
 *   <li>3.1 text：DEFAULT 为 {@code 默认}、PROD 为 {@code 生产}（text）</li>
 * </ul>
 *
 * @since 2026/1/14
 * @version 1.0
 */
public class CProfileEnumTests {

    /**
     * 测试按名称获取环境枚举（大小写不敏感）
     * 对应测试用例 1.1：大小写不敏感：{@code default}/{@code DEFAULT} 均返回 DEFAULT
     */
    @Test
    public void of() {

        Assertions.assertEquals(CProfileEnum.DEFAULT, CProfileEnum.of("default"));
        Assertions.assertEquals(CProfileEnum.DEFAULT, CProfileEnum.of("DEFAULT"));

    }

    /**
     * 测试全部环境枚举按名称获取
     * 对应测试用例 1.2：全枚举：6 个环境按名获取
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
     * 对应测试用例 1.3：未知异常：{@code UNKNOWN}/{@code prod1}/空/null 抛 IllegalArgumentException
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
     * 对应测试用例 2.1：大小 1 仅含 PROD、不含 DEV
     */
    @Test
    public void prodProfiles() {

        Assertions.assertEquals(1, CProfileEnum.PROD_PROFILES.size());
        Assertions.assertTrue(CProfileEnum.PROD_PROFILES.contains(CProfileEnum.PROD));
        Assertions.assertFalse(CProfileEnum.PROD_PROFILES.contains(CProfileEnum.DEV));

    }

    /**
     * 测试描述字段
     * 对应测试用例 3.1：DEFAULT 为 {@code 默认}、PROD 为 {@code 生产}
     */
    @Test
    public void text() {

        Assertions.assertEquals("默认", CProfileEnum.DEFAULT.getText());
        Assertions.assertEquals("生产", CProfileEnum.PROD.getText());

    }

}
