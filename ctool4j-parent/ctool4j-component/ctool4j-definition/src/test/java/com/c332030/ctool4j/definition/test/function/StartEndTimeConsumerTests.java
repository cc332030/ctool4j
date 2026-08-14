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
 * @since 2026/8/14
 */
public class StartEndTimeConsumerTests {

    @Test
    public void acceptNormal() {

        AtomicInteger holder = new AtomicInteger(0);

        StartEndTimeConsumer consumer = (start, end) -> holder.set(1);

        consumer.accept(Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-02T00:00:00Z"));

        Assertions.assertEquals(1, holder.get());

    }

    @Test
    public void acceptNullInputs() {

        AtomicInteger holder = new AtomicInteger(0);

        StartEndTimeConsumer consumer = (start, end) -> holder.set(1);

        consumer.accept(null, null);

        Assertions.assertEquals(1, holder.get());

    }

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
