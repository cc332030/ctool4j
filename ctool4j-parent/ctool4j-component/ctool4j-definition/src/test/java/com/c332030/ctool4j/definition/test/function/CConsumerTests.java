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
 * @since 2026/8/14
 */
public class CConsumerTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void acceptNormal() {

        AtomicInteger holder = new AtomicInteger(0);

        CConsumer<Integer> consumer = holder::set;

        consumer.accept(5);

        Assertions.assertEquals(5, holder.get());

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void acceptNullInput() {

        AtomicInteger holder = new AtomicInteger(0);

        CConsumer<String> consumer = s -> holder.incrementAndGet();

        consumer.accept(null);

        Assertions.assertEquals(1, holder.get());

    }

    /**
     * 对应测试用例 1.3
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
     * 对应测试用例 2.1
     */
    @Test
    public void empty() {

        CConsumer<String> empty = CConsumer.empty();

        Assertions.assertDoesNotThrow(() -> empty.accept("abc"));

    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void staticAcceptNullConsumer() {

        Assertions.assertDoesNotThrow(() -> CConsumer.accept(null, "input"));

    }

    /**
     * 对应测试用例 2.3
     */
    @Test
    public void staticAcceptNormal() {

        AtomicInteger holder = new AtomicInteger(0);

        Consumer<Integer> consumer = holder::set;

        CConsumer.accept(consumer, 7);

        Assertions.assertEquals(7, holder.get());

    }

    /**
     * 对应测试用例 2.4
     */
    @Test
    public void convert() {

        AtomicInteger holder = new AtomicInteger(0);

        CConsumer<Integer> cConsumer = holder::set;

        Consumer<Integer> consumer = CConsumer.convert(cConsumer);

        consumer.accept(9);

        Assertions.assertEquals(9, holder.get());

    }

    /**
     * 对应测试用例 2.5
     */
    @Test
    public void convertNullCConsumer() {

        Consumer<Integer> consumer = CConsumer.convert(null);

        Assertions.assertDoesNotThrow(() -> consumer.accept(1));

    }

}
