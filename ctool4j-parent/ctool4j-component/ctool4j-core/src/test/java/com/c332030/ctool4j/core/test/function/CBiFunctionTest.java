package com.c332030.ctool4j.core.test.function;

import com.c332030.ctool4j.definition.function.CBiFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CBiFunctionTest
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>白盒原则：围绕 CBiFunction 内置常量 {@code FIRST}/{@code SECOND}（取第一/第二个参数）的行为设计用例，覆盖不同参数类型（Integer/String）。</li>
 * </ul>
 * <h2>内置函数常量</h2>
 * <ul>
 *   <li>2.1 first：取第一个参数（Integer/String）</li>
 *   <li>2.2 second：取第二个参数（Integer/String）</li>
 * </ul>
 *
 * @since 2025/11/6
 * @version 1.0
 */
public class CBiFunctionTest {

    /**
     * 测试取第一个参数
     * 对应测试用例 2.1：取第一个参数（Integer/String）
     */
    @Test
    public void first() {

        Assertions.assertEquals(1, CBiFunction.FIRST.apply(1, 2));
        Assertions.assertEquals("1", CBiFunction.FIRST.apply("1", "2"));

    }

    /**
     * 测试取第二个参数
     * 对应测试用例 2.2：取第二个参数（Integer/String）
     */
    @Test
    public void second() {

        Assertions.assertEquals(2, CBiFunction.SECOND.apply(1, 2));
        Assertions.assertEquals("2", CBiFunction.SECOND.apply("1", "2"));

    }

}
