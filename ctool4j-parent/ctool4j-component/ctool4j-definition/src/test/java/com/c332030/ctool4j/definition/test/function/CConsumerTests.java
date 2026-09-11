package com.c332030.ctool4j.definition.test.function;

import com.c332030.ctool4j.definition.function.CConsumer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * <p>
 * Description: CConsumerTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「accept / 工具方法」两个维度组织。</li>
 *   <li>accept 覆盖正常、null 输入、受检异常；工具覆盖 empty、静态 accept（null/正常）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 @SneakyThrows 包装与工具方法的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：accept 正常/null/受检异常；empty；静态 accept null/正常。</li>
 *   <li>未覆盖：无。</li>
 * </ul>
 * <h2>accept</h2>
 * <ul>
 *   <li>1.1 正常：消费生效（acceptNormal）</li>
 *   <li>1.2 null 输入：正常处理（acceptNullInput）</li>
 *   <li>1.3 受检异常：抛 IOException（acceptSneakyThrowsCheckedException）</li>
 * </ul>
 * <h2>工具方法</h2>
 * <ul>
 *   <li>2.1 empty：不抛异常（empty）</li>
 *   <li>2.2 静态 accept null：不抛异常（staticAcceptNullConsumer）</li>
 *   <li>2.3 静态 accept 正常：消费生效（staticAcceptNormal）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CConsumerTests {

    /**
     * 对应测试用例 1.1：正常：消费生效
     */
    @Test
    public void acceptNormal() {

        AtomicInteger holder = new AtomicInteger(0);

        CConsumer<Integer> consumer = holder::set;

        consumer.accept(5);

        Assertions.assertEquals(5, holder.get());

    }

    /**
     * 对应测试用例 1.2：null 输入：正常处理
     */
    @Test
    public void acceptNullInput() {

        AtomicInteger holder = new AtomicInteger(0);

        CConsumer<String> consumer = s -> holder.incrementAndGet();

        consumer.accept(null);

        Assertions.assertEquals(1, holder.get());

    }

    /**
     * 对应测试用例 1.3：受检异常：抛 IOException
     */
    @Test
    public void acceptSneakyThrowsCheckedException() {

        CConsumer<String> consumer = s -> {
            throw new IOException("io error");
        };

        Assertions.assertThrowsExactly(
            IOException.class,
            () -> consumer.accept("input")
        );

    }

    /**
     * 对应测试用例 2.1：不抛异常
     */
    @Test
    public void empty() {

        CConsumer<String> empty = CConsumer.empty();

        Assertions.assertDoesNotThrow(() -> empty.accept("abc"));

    }

    /**
     * 对应测试用例 2.2：静态 accept null：不抛异常
     */
    @Test
    public void staticAcceptNullConsumer() {

        Assertions.assertDoesNotThrow(() -> CConsumer.accept(null, "input"));

    }

    /**
     * 对应测试用例 2.3：静态 accept 正常：消费生效
     */
    @Test
    public void staticAcceptNormal() {

        AtomicInteger holder = new AtomicInteger(0);

        Consumer<Integer> consumer = holder::set;

        CConsumer.accept(consumer, 7);

        Assertions.assertEquals(7, holder.get());

    }

}
