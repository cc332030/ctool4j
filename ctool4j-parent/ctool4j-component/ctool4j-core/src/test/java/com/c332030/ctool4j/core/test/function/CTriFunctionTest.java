package com.c332030.ctool4j.core.test.function;

import com.c332030.ctool4j.definition.function.CTriFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CTriFunctionTest
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>白盒原则：围绕 CTriFunction 内置常量（取第一/第二/第三个参数）的行为设计用例，覆盖不同参数类型。</li>
 * </ul>
 * <h2>内置函数常量</h2>
 * <ul>
 *   <li>2.1 取第一个参数</li>
 *   <li>2.2 取第二个参数</li>
 *   <li>2.3 取第三个参数</li>
 * </ul>
 *
 * @since 2025/11/6
 * @version 1.0
 */
public class CTriFunctionTest {

    /**
     * 测试取第一个参数
     * 对应测试用例 2.1：取第一个参数
     */
    @Test
    public void first() {

        Assertions.assertEquals(1, CTriFunction.FIRST.apply(1, 2, 3));
        Assertions.assertEquals("1", CTriFunction.FIRST.apply("1", "2", "3"));

    }

    /**
     * 测试取第二个参数
     * 对应测试用例 2.2：取第二个参数
     */
    @Test
    public void second() {

        Assertions.assertEquals(2, CTriFunction.SECOND.apply(1, 2, 3));
        Assertions.assertEquals("2", CTriFunction.SECOND.apply("1", "2", "3"));

    }

    /**
     * 测试取第三个参数
     * 对应测试用例 2.3：取第三个参数
     */
    @Test
    public void third() {

        Assertions.assertEquals(3, CTriFunction.THIRD.apply(1, 2, 3));
        Assertions.assertEquals("3", CTriFunction.THIRD.apply("1", "2", "3"));

    }

}
