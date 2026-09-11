package com.c332030.ctool4j.definition.test.function;

import com.c332030.ctool4j.definition.function.CFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.Function;

/**
 * <p>
 * Description: CFunctionTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「apply / 工具方法」两个维度组织。</li>
 *   <li>apply 覆盖正常、null 输入、受检异常（@SneakyThrows）、运行时异常。</li>
 *   <li>工具覆盖 self/empty、静态 apply（null/正常）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 @SneakyThrows 包装与工具方法的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：apply 正常/null/受检异常/运行时异常；self；empty；静态 apply null/正常。</li>
 *   <li>未覆盖：无（覆盖了核心行为）。</li>
 * </ul>
 * <h2>apply</h2>
 * <ul>
 *   <li>1.1 正常：Integer::valueOf 转换（applyNormal）</li>
 *   <li>1.2 null 输入：lambda 正常处理 null（applyNullInput）</li>
 *   <li>1.3 受检异常：抛 IOException（applySneakyThrowsCheckedException）</li>
 *   <li>1.4 运行时异常：抛 IllegalArgumentException（applySneakyThrowsRuntimeException）</li>
 * </ul>
 * <h2>工具方法</h2>
 * <ul>
 *   <li>2.1 self：返回自身（self）</li>
 *   <li>2.2 empty：恒返回 null（empty）</li>
 *   <li>2.3 静态 apply null：返回 null（staticApplyNullFunction / staticApplyNullFunctionWithNullInput）</li>
 *   <li>2.4 静态 apply 正常：转换正确（staticApplyNormal）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CFunctionTests {

    /**
     * 对应测试用例 1.1：正常：Integer::valueOf 转换
     */
    @Test
    public void applyNormal() {

        CFunction<String, Integer> function = Integer::valueOf;

        Assertions.assertEquals(Integer.valueOf(123), function.apply("123"));

    }

    /**
     * 对应测试用例 1.2：null 输入：lambda 正常处理 null
     */
    @Test
    public void applyNullInput() {

        CFunction<String, String> function = s -> "x";

        Assertions.assertEquals("x", function.apply(null));

    }

    /**
     * 对应测试用例 1.3：受检异常：抛 IOException
     */
    @Test
    public void applySneakyThrowsCheckedException() {

        CFunction<String, String> function = s -> {
            throw new IOException("io error");
        };

        Assertions.assertThrowsExactly(
            IOException.class,
            () -> function.apply("input")
        );

    }

    /**
     * 对应测试用例 1.4：运行时异常：抛 IllegalArgumentException
     */
    @Test
    public void applySneakyThrowsRuntimeException() {

        CFunction<String, String> function = s -> {
            throw new IllegalArgumentException("bad arg");
        };

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> function.apply("input")
        );

    }

    /**
     * 对应测试用例 2.1：返回自身
     */
    @Test
    public void self() {

        CFunction<String, String> self = CFunction.self();

        Assertions.assertEquals("abc", self.apply("abc"));
        Assertions.assertNull(self.apply(null));

    }

    /**
     * 对应测试用例 2.2：恒返回 null
     */
    @Test
    public void empty() {

        CFunction<String, String> empty = CFunction.empty();

        Assertions.assertNull(empty.apply("abc"));

    }

    /**
     * 对应测试用例 2.3：静态 apply null：返回 null（staticApplyNullFunction / staticApplyNullFunctionWithNullInput）
     */
    @Test
    public void staticApplyNullFunction() {

        Assertions.assertNull(CFunction.apply(null, "input"));

    }

    /**
     * 对应测试用例 2.3：静态 apply null：返回 null（staticApplyNullFunction / staticApplyNullFunctionWithNullInput）
     */
    @Test
    public void staticApplyNullFunctionWithNullInput() {

        Assertions.assertNull(CFunction.apply(null, null));

    }

    /**
     * 对应测试用例 2.4：静态 apply 正常：转换正确
     */
    @Test
    public void staticApplyNormal() {

        Function<String, Integer> function = Integer::valueOf;

        Assertions.assertEquals(Integer.valueOf(123), CFunction.apply(function, "123"));

    }

}
