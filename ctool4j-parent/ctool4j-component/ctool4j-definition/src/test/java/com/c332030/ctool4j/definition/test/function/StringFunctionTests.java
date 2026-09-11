package com.c332030.ctool4j.definition.test.function;

import com.c332030.ctool4j.definition.function.StringFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * <p>
 * Description: StringFunctionTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证 StringFunction 的 apply 正常、null 输入、受检异常。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对继承 CFunction 行为的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：apply 正常/null/受检异常。</li>
 *   <li>未覆盖：无。</li>
 * </ul>
 * <h2>apply</h2>
 * <ul>
 *   <li>1.1 正常：结果正确（applyNormal）</li>
 *   <li>1.2 null 输入：正常处理（applyNullInput）</li>
 *   <li>1.3 受检异常：抛 IOException（applySneakyThrowsCheckedException）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class StringFunctionTests {

    /**
     * 对应测试用例 1.1：正常：结果正确
     */
    @Test
    public void applyNormal() {

        StringFunction<Integer> function = Integer::valueOf;

        Assertions.assertEquals(Integer.valueOf(123), function.apply("123"));

    }

    /**
     * 对应测试用例 1.2：null 输入：正常处理
     */
    @Test
    public void applyNullInput() {

        StringFunction<String> function = s -> s + "x";

        Assertions.assertEquals("nullx", function.apply(null));

    }

    /**
     * 对应测试用例 1.3：受检异常：抛 IOException
     */
    @Test
    public void applySneakyThrowsCheckedException() {

        StringFunction<String> function = s -> {
            throw new IOException("io error");
        };

        Assertions.assertThrowsExactly(
            IOException.class,
            () -> function.apply("input")
        );

    }

}
