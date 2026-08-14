package com.c332030.ctool4j.feign.test.enums;

import com.c332030.ctool4j.feign.enums.CFeignClientHeaderPropagationModeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * CFeignClientHeaderPropagationModeEnum 测试
 *
 * 覆盖：getText / values / valueOf 正常路径与异常路径
 *
 * @author c332030
 */
class CFeignClientHeaderPropagationModeEnumTests {

    @Test
    void testGetText() {

        Assertions.assertEquals("全部", CFeignClientHeaderPropagationModeEnum.ALL.getText());
        Assertions.assertEquals("自定义", CFeignClientHeaderPropagationModeEnum.CUSTOM.getText());
        Assertions.assertEquals("无", CFeignClientHeaderPropagationModeEnum.NONE.getText());
    }

    @Test
    void testValues() {

        Assertions.assertEquals(3, CFeignClientHeaderPropagationModeEnum.values().length);
    }

    @Test
    void testValueOf() {

        Assertions.assertSame(CFeignClientHeaderPropagationModeEnum.ALL, CFeignClientHeaderPropagationModeEnum.valueOf("ALL"));
    }

    @Test
    void testValueOfNotExist() {

        // 枚举名不存在时，valueOf 抛 IllegalArgumentException
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> CFeignClientHeaderPropagationModeEnum.valueOf("NOT_EXIST"));
    }
}
