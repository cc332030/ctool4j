package com.c332030.ctool4j.feign.test.enums;

import com.c332030.ctool4j.feign.enums.CFeignClientHeaderPropagationModeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * CFeignClientHeaderPropagationModeEnum 测试
 *
 * 覆盖：getText / values / valueOf 正常路径与异常路径
 *
 * <h2>用例设计思路与依据</h2>
 * <ul>
 *   <li>验证枚举 getText、values、valueOf 正常与不存在抛异常。</li>
 * </ul>
 * <h2>枚举访问</h2>
 * <ul>
 *   <li>1.1 getText 中文描述（testGetText）</li>
 *   <li>1.2 values 枚举数量（testValues）</li>
 *   <li>1.3 valueOf 正常（testValueOf）</li>
 *   <li>1.4 valueOf 不存在抛 IllegalArgumentException（testValueOfNotExist）</li>
 * </ul>
 *
 * @author c332030
 * @since 1.0
 * @version 1.0
 */
class CFeignClientHeaderPropagationModeEnumTests {

    /**
     * 对应测试用例 1.1：getText 中文描述
     */
    @Test
    void testGetText() {

        Assertions.assertEquals("全部", CFeignClientHeaderPropagationModeEnum.ALL.getText());
        Assertions.assertEquals("自定义", CFeignClientHeaderPropagationModeEnum.CUSTOM.getText());
        Assertions.assertEquals("无", CFeignClientHeaderPropagationModeEnum.NONE.getText());
    }

    /**
     * 对应测试用例 1.2：values 枚举数量
     */
    @Test
    void testValues() {

        Assertions.assertEquals(3, CFeignClientHeaderPropagationModeEnum.values().length);
    }

    /**
     * 对应测试用例 1.3：valueOf 正常
     */
    @Test
    void testValueOf() {

        Assertions.assertSame(CFeignClientHeaderPropagationModeEnum.ALL, CFeignClientHeaderPropagationModeEnum.valueOf("ALL"));
    }

    /**
     * 对应测试用例 1.4：valueOf 不存在抛 IllegalArgumentException
     */
    @Test
    void testValueOfNotExist() {

        // 枚举名不存在时，valueOf 抛 IllegalArgumentException
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> CFeignClientHeaderPropagationModeEnum.valueOf("NOT_EXIST"));
    }
}
