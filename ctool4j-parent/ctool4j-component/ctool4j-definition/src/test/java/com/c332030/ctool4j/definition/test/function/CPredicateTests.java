package com.c332030.ctool4j.definition.test.function;

import com.c332030.ctool4j.definition.function.CPredicate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.Predicate;

/**
 * <p>
 * Description: CPredicateTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「test / 工具方法」两个维度组织。</li>
 *   <li>test 覆盖正常、null 输入、受检异常；工具覆盖 alwaysTrue/alwaysFalse、静态 test（null/正常）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 @SneakyThrows 包装与工具方法的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：test 正常/null/受检异常；alwaysTrue/alwaysFalse；静态 test null/正常。</li>
 *   <li>未覆盖：无。</li>
 * </ul>
 * <h2>test</h2>
 * <ul>
 *   <li>1.1 正常：正反断言（testNormal）</li>
 *   <li>1.2 null 输入：正常处理（testNullInput）</li>
 *   <li>1.3 受检异常：抛 IOException（testSneakyThrowsCheckedException）</li>
 * </ul>
 * <h2>工具方法</h2>
 * <ul>
 *   <li>2.1 alwaysTrue：恒 true（alwaysTrue）</li>
 *   <li>2.2 alwaysFalse：恒 false（alwaysFalse）</li>
 *   <li>2.3 静态 test null：返回 false（staticTestNullPredicate）</li>
 *   <li>2.4 静态 test 正常：结果正确（staticTestNormal）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CPredicateTests {

    /**
     * 对应测试用例 1.1：正常：正反断言
     */
    @Test
    public void testNormal() {

        CPredicate<Integer> predicate = i -> i > 0;

        Assertions.assertTrue(predicate.test(1));
        Assertions.assertFalse(predicate.test(-1));

    }

    /**
     * 对应测试用例 1.2：null 输入：正常处理
     */
    @Test
    public void testNullInput() {

        CPredicate<String> predicate = s -> true;

        Assertions.assertTrue(predicate.test(null));

    }

    /**
     * 对应测试用例 1.3：受检异常：抛 IOException
     */
    @Test
    public void testSneakyThrowsCheckedException() {

        CPredicate<String> predicate = s -> {
            throw new IOException("io error");
        };

        Assertions.assertThrowsExactly(
            IOException.class,
            () -> predicate.test("input")
        );

    }

    /**
     * 对应测试用例 2.1：恒 true
     */
    @Test
    public void alwaysTrue() {

        CPredicate<String> predicate = CPredicate.alwaysTrue();

        Assertions.assertTrue(predicate.test(null));
        Assertions.assertTrue(predicate.test("abc"));

    }

    /**
     * 对应测试用例 2.2：恒 false
     */
    @Test
    public void alwaysFalse() {

        CPredicate<String> predicate = CPredicate.alwaysFalse();

        Assertions.assertFalse(predicate.test(null));
        Assertions.assertFalse(predicate.test("abc"));

    }

    /**
     * 对应测试用例 2.3：静态 test null：返回 false
     */
    @Test
    public void staticTestNullPredicate() {

        Assertions.assertFalse(CPredicate.test(null, "input"));

    }

    /**
     * 对应测试用例 2.4：静态 test 正常：结果正确
     */
    @Test
    public void staticTestNormal() {

        Predicate<Integer> predicate = i -> i > 0;

        Assertions.assertTrue(CPredicate.test(predicate, 1));
        Assertions.assertFalse(CPredicate.test(predicate, -1));

    }

}
