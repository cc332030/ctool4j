package com.c332030.ctool4j.definition.test.function;

import com.c332030.ctool4j.definition.function.StartEndTimeConsumer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Description: StartEndTimeConsumerTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证 acceptThrowable 的正常消费、null 输入与受检异常。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对消费起止时间的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：accept 正常/null/受检异常。</li>
 *   <li>未覆盖：无。</li>
 * </ul>
 * <h2>accept</h2>
 * <ul>
 *   <li>1.1 正常：消费起止时间（acceptNormal）</li>
 *   <li>1.2 null 输入：正常处理（acceptNullInputs）</li>
 *   <li>1.3 受检异常：抛 IOException（acceptSneakyThrowsCheckedException）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class StartEndTimeConsumerTests {

    /**
     * 对应测试用例 1.1：正常：消费起止时间
     */
    @Test
    public void acceptNormal() {

        AtomicInteger holder = new AtomicInteger(0);

        StartEndTimeConsumer consumer = (start, end) -> holder.set(1);

        consumer.accept(Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-02T00:00:00Z"));

        Assertions.assertEquals(1, holder.get());

    }

    /**
     * 对应测试用例 1.2：null 输入：正常处理
     */
    @Test
    public void acceptNullInputs() {

        AtomicInteger holder = new AtomicInteger(0);

        StartEndTimeConsumer consumer = (start, end) -> holder.set(1);

        consumer.accept(null, null);

        Assertions.assertEquals(1, holder.get());

    }

    /**
     * 对应测试用例 1.3：受检异常：抛 IOException
     */
    @Test
    public void acceptSneakyThrowsCheckedException() {

        StartEndTimeConsumer consumer = (start, end) -> {
            throw new IOException("io error");
        };

        Assertions.assertThrowsExactly(
            IOException.class,
            () -> consumer.accept(Instant.now(), Instant.now())
        );

    }

}
