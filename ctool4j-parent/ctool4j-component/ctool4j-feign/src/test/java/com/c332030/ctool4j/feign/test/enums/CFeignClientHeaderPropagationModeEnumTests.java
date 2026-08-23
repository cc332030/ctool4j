package com.c332030.ctool4j.feign.test.enums;

import com.c332030.ctool4j.feign.enums.CFeignClientHeaderPropagationModeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * CFeignClientHeaderPropagationModeEnum 测试
 *
 * 覆盖：getText / values / valueOf 正常路径与异常路径
 *
 * <p>是 {@link CFeignClientHeaderPropagationModeEnum} 的测试用例（对应测试文档
 * <code>doc/design/feign/CFeignClientHeaderPropagationModeEnumTests.adoc</code>）。</p>
 *
 * @author c332030
 */
class CFeignClientHeaderPropagationModeEnumTests {

    /** 对应测试用例 1.1 */
    @Test
    void testGetText() {

        Assertions.assertEquals("全部", CFeignClientHeaderPropagationModeEnum.ALL.getText());
        Assertions.assertEquals("自定义", CFeignClientHeaderPropagationModeEnum.CUSTOM.getText());
        Assertions.assertEquals("无", CFeignClientHeaderPropagationModeEnum.NONE.getText());
    }

    /** 对应测试用例 1.2 */
    @Test
    void testValues() {

        Assertions.assertEquals(3, CFeignClientHeaderPropagationModeEnum.values().length);
    }

    /** 对应测试用例 1.3 */
    @Test
    void testValueOf() {

        Assertions.assertSame(CFeignClientHeaderPropagationModeEnum.ALL, CFeignClientHeaderPropagationModeEnum.valueOf("ALL"));
    }

    /** 对应测试用例 1.4 */
    @Test
    void testValueOfNotExist() {

        // 枚举名不存在时，valueOf 抛 IllegalArgumentException
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> CFeignClientHeaderPropagationModeEnum.valueOf("NOT_EXIST"));
    }
}
