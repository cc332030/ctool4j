package com.c332030.ctool4j.definition.test.enums.business;

import com.c332030.ctool4j.definition.enums.business.CCountryCodeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CCountryCodeEnumTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证枚举数量、CHN 的 value/text/name，以及未知名抛异常。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对枚举元素与描述的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：枚举数量；CHN 值/描述/名称；未知名抛异常。</li>
 *   <li>未覆盖：无。</li>
 * </ul>
 * <h2>枚举</h2>
 * <ul>
 *   <li>1.1 values：1 个枚举（values）</li>
 *   <li>1.2 chn：value 86/{@code 中国}/CHN（chn）</li>
 *   <li>1.3 未知名抛异常（valueOfUnknown）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CCountryCodeEnumTests {

    /**
     * 对应测试用例 1.1：1 个枚举
     */
    @Test
    public void values() {

        Assertions.assertEquals(1, CCountryCodeEnum.values().length);

    }

    /**
     * 对应测试用例 1.2：value 86/{@code 中国}/CHN
     */
    @Test
    public void chn() {

        Assertions.assertEquals(Integer.valueOf(86), CCountryCodeEnum.CHN.getValue());
        Assertions.assertEquals("中国", CCountryCodeEnum.CHN.getText());
        Assertions.assertEquals("CHN", CCountryCodeEnum.CHN.name());

    }

    /**
     * 对应测试用例 1.3：未知名抛异常
     */
    @Test
    public void valueOfUnknown() {

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CCountryCodeEnum.valueOf("USA")
        );

    }

}
