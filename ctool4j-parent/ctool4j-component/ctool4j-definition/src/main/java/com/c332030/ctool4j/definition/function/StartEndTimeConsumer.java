package com.c332030.ctool4j.definition.function;

import java.time.Instant;

/**
 * <p>
 * Description: StartEndTimeConsumer
 * </p>
 *
 * @since 2025/10/31
 * @see doc/design/core/StartEndTimeConsumer.adoc
 * @see doc/design/core/StartEndTimeConsumerTests.adoc
 */
@FunctionalInterface
public interface StartEndTimeConsumer extends CBiConsumer<Instant, Instant> {

    /**
     * 消费起止时间，可抛出受检异常
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @throws Throwable 处理过程中可能抛出的异常
     */
    @Override
    void acceptThrowable(Instant startTime, Instant endTime) throws Throwable;

}
