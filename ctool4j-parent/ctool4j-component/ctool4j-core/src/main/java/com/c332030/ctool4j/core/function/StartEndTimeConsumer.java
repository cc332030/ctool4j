package com.c332030.ctool4j.core.function;

import java.time.Instant;

/**
 * <p>
 * Description: StartEndTimeConsumer
 * </p>
 *
 * @since 2025/10/31
 */
@FunctionalInterface
public interface StartEndTimeConsumer extends CBiConsumer<Instant, Instant> {

    @Override
    void acceptThrowable(Instant startTime, Instant endTime) throws Throwable;

}
