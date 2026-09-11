package com.c332030.ctool4j.definition.test.enums.common;

import com.c332030.ctool4j.definition.enums.common.CVersionEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CVersionEnumTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证枚举数量、各版本描述/名称、valueOf 正常与未知名抛异常。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对枚举元素与描述的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：枚举数量；V1/V2/V3 描述与名称；valueOf 正常；未知名抛异常。</li>
 *   <li>未覆盖：无。</li>
 * </ul>
 * <h2>枚举</h2>
 * <ul>
 *   <li>1.1 values：3 个枚举（values）</li>
 *   <li>1.2 v1：{@code V1}/V1（v1）</li>
 *   <li>1.3 v2：{@code V2}/V2（v2）</li>
 *   <li>1.4 v3：{@code V3}/V3（v3）</li>
 *   <li>1.5 valueOf 正常（valueOf_normal）</li>
 *   <li>1.6 未知名抛异常（valueOfUnknown）</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.0
 */
public class CVersionEnumTests {

    /**
     * 对应测试用例 1.1：3 个枚举
     */
    @Test
    public void values() {

        Assertions.assertEquals(3, CVersionEnum.values().length);

    }

    /**
     * 对应测试用例 1.2：{@code V1}/V1
     */
    @Test
    public void v1() {

        Assertions.assertEquals("V1", CVersionEnum.V1.getText());
        Assertions.assertEquals("V1", CVersionEnum.V1.name());

    }

    /**
     * 对应测试用例 1.3：{@code V2}/V2
     */
    @Test
    public void v2() {

        Assertions.assertEquals("V2", CVersionEnum.V2.getText());
        Assertions.assertEquals("V2", CVersionEnum.V2.name());

    }

    /**
     * 对应测试用例 1.4：{@code V3}/V3
     */
    @Test
    public void v3() {

        Assertions.assertEquals("V3", CVersionEnum.V3.getText());
        Assertions.assertEquals("V3", CVersionEnum.V3.name());

    }

    /**
     * 对应测试用例 1.5：valueOf 正常
     */
    @Test
    public void valueOf_normal() {

        Assertions.assertSame(CVersionEnum.V1, CVersionEnum.valueOf("V1"));
        Assertions.assertSame(CVersionEnum.V2, CVersionEnum.valueOf("V2"));
        Assertions.assertSame(CVersionEnum.V3, CVersionEnum.valueOf("V3"));

    }

    /**
     * 对应测试用例 1.6：未知名抛异常
     */
    @Test
    public void valueOfUnknown() {

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CVersionEnum.valueOf("UNKNOWN")
        );

    }

}
