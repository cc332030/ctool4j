package com.c332030.ctool4j.definition.test.function;

import com.c332030.ctool4j.definition.function.CBiConsumer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.BiConsumer;

/**
 * <p>
 * Description: CBiConsumerTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CBiConsumerTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void acceptNormal() {

        StringBuilder sb = new StringBuilder();

        CBiConsumer<String, String> consumer = (a, b) -> sb.append(a).append(b);

        consumer.accept("a", "b");

        Assertions.assertEquals("ab", sb.toString());

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void acceptNullInputs() {

        StringBuilder sb = new StringBuilder();

        CBiConsumer<String, String> consumer = (a, b) -> sb.append(a).append(b);

        consumer.accept(null, null);

        Assertions.assertEquals("nullnull", sb.toString());

    }

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void acceptSneakyThrowsCheckedException() {

        CBiConsumer<String, String> consumer = (a, b) -> {
            throw new IOException("io error");
        };

        Assertions.assertThrowsExactly(
            IOException.class,
            () -> consumer.accept("a", "b")
        );

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void empty() {

        CBiConsumer<String, String> empty = CBiConsumer.empty();

        Assertions.assertDoesNotThrow(() -> empty.accept("a", "b"));

    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void staticAcceptNullConsumer() {

        Assertions.assertDoesNotThrow(() -> CBiConsumer.accept(null, "a", "b"));

    }

    /**
     * 对应测试用例 2.3
     */
    @Test
    public void staticAcceptNormal() {

        StringBuilder sb = new StringBuilder();

        BiConsumer<String, String> consumer = (a, b) -> sb.append(a).append(b);

        CBiConsumer.accept(consumer, "x", "y");

        Assertions.assertEquals("xy", sb.toString());

    }

    /**
     * 对应测试用例 2.4
     */
    @Test
    public void convert() {

        StringBuilder sb = new StringBuilder();

        CBiConsumer<String, String> cConsumer = (a, b) -> sb.append(a).append(b);

        BiConsumer<String, String> consumer = CBiConsumer.convert(cConsumer);

        consumer.accept("m", "n");

        Assertions.assertEquals("mn", sb.toString());

    }

    /**
     * 对应测试用例 2.5
     */
    @Test
    public void convertNullCBiConsumer() {

        BiConsumer<String, String> consumer = CBiConsumer.convert(null);

        Assertions.assertDoesNotThrow(() -> consumer.accept("m", "n"));

    }

}
