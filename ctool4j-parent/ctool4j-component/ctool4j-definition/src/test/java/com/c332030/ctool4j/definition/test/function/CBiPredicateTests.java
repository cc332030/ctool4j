package com.c332030.ctool4j.definition.test.function;

import com.c332030.ctool4j.definition.function.CBiPredicate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.BiPredicate;

/**
 * <p>
 * Description: CBiPredicateTests
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
 *   <li>1.2 null 输入：正常处理（testNullInputs）</li>
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
public class CBiPredicateTests {

    /**
     * 对应测试用例 1.1：正常：正反断言
     */
    @Test
    public void testNormal() {

        CBiPredicate<Integer, Integer> predicate = (a, b) -> a > b;

        Assertions.assertTrue(predicate.test(2, 1));
        Assertions.assertFalse(predicate.test(1, 2));

    }

    /**
     * 对应测试用例 1.2：null 输入：正常处理
     */
    @Test
    public void testNullInputs() {

        CBiPredicate<String, String> predicate = (a, b) -> true;

        Assertions.assertTrue(predicate.test(null, null));

    }

    /**
     * 对应测试用例 1.3：受检异常：抛 IOException
     */
    @Test
    public void testSneakyThrowsCheckedException() {

        CBiPredicate<String, String> predicate = (a, b) -> {
            throw new IOException("io error");
        };

        Assertions.assertThrowsExactly(
            IOException.class,
            () -> predicate.test("a", "b")
        );

    }

    /**
     * 对应测试用例 2.1：恒 true
     */
    @Test
    public void alwaysTrue() {

        CBiPredicate<String, String> predicate = CBiPredicate.alwaysTrue();

        Assertions.assertTrue(predicate.test(null, null));
        Assertions.assertTrue(predicate.test("a", "b"));

    }

    /**
     * 对应测试用例 2.2：恒 false
     */
    @Test
    public void alwaysFalse() {

        CBiPredicate<String, String> predicate = CBiPredicate.alwaysFalse();

        Assertions.assertFalse(predicate.test(null, null));
        Assertions.assertFalse(predicate.test("a", "b"));

    }

    /**
     * 对应测试用例 2.3：静态 test null：返回 false
     */
    @Test
    public void staticTestNullPredicate() {

        Assertions.assertFalse(CBiPredicate.test(null, "a", "b"));

    }

    /**
     * 对应测试用例 2.4：静态 test 正常：结果正确
     */
    @Test
    public void staticTestNormal() {

        BiPredicate<Integer, Integer> predicate = (a, b) -> a > b;

        Assertions.assertTrue(CBiPredicate.test(predicate, 2, 1));
        Assertions.assertFalse(CBiPredicate.test(predicate, 1, 2));

    }

}
