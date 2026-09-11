package com.c332030.ctool4j.definition.test.function;

import com.c332030.ctool4j.definition.function.CBiFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.BiFunction;

/**
 * <p>
 * Description: CBiFunctionTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「apply / 工具方法」两个维度组织。</li>
 *   <li>apply 覆盖正常、null 输入、受检异常；工具覆盖 first/second、静态 apply（null/正常）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 @SneakyThrows 包装与工具方法的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：apply 正常/null/受检异常；first/second；静态 apply null/正常。</li>
 *   <li>未覆盖：无。</li>
 * </ul>
 * <h2>apply</h2>
 * <ul>
 *   <li>1.1 正常：结果正确（applyNormal）</li>
 *   <li>1.2 null 输入：正常处理（applyNullInputs）</li>
 *   <li>1.3 受检异常：抛 IOException（applySneakyThrowsCheckedException）</li>
 * </ul>
 * <h2>工具方法</h2>
 * <ul>
 *   <li>2.1 first：返回第一参数（first）</li>
 *   <li>2.2 second：返回第二参数（second）</li>
 *   <li>2.3 静态 apply null：返回 null（staticApplyNullFunction）</li>
 *   <li>2.4 静态 apply 正常：结果正确（staticApplyNormal）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CBiFunctionTests {

    /**
     * 对应测试用例 1.1：正常：结果正确
     */
    @Test
    public void applyNormal() {

        CBiFunction<Integer, Integer, Integer> function = Integer::sum;

        Assertions.assertEquals(Integer.valueOf(3), function.apply(1, 2));

    }

    /**
     * 对应测试用例 1.2：null 输入：正常处理
     */
    @Test
    public void applyNullInputs() {

        CBiFunction<String, String, String> function = (a, b) -> a + b;

        Assertions.assertEquals("nullnull", function.apply(null, null));

    }

    /**
     * 对应测试用例 1.3：受检异常：抛 IOException
     */
    @Test
    public void applySneakyThrowsCheckedException() {

        CBiFunction<String, String, String> function = (a, b) -> {
            throw new IOException("io error");
        };

        Assertions.assertThrowsExactly(
            IOException.class,
            () -> function.apply("a", "b")
        );

    }

    /**
     * 对应测试用例 2.1：返回第一参数
     */
    @Test
    public void first() {

        BiFunction<Integer, Integer, Integer> first = CBiFunction.first();

        Assertions.assertEquals(Integer.valueOf(1), first.apply(1, 2));

    }

    /**
     * 对应测试用例 2.2：返回第二参数
     */
    @Test
    public void second() {

        BiFunction<Integer, Integer, Integer> second = CBiFunction.second();

        Assertions.assertEquals(Integer.valueOf(2), second.apply(1, 2));

    }

    /**
     * 对应测试用例 2.3：静态 apply null：返回 null
     */
    @Test
    public void staticApplyNullFunction() {

        Assertions.assertNull(CBiFunction.apply(null, 1, 2));

    }

    /**
     * 对应测试用例 2.4：静态 apply 正常：结果正确
     */
    @Test
    public void staticApplyNormal() {

        BiFunction<Integer, Integer, Integer> function = Integer::sum;

        Assertions.assertEquals(Integer.valueOf(3), CBiFunction.apply(function, 1, 2));

    }

}
