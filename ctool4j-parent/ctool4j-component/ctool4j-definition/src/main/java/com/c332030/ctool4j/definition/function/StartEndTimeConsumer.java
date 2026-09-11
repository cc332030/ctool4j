package com.c332030.ctool4j.definition.function;

import java.time.Instant;

/**
 * <p>
 * Description: StartEndTimeConsumer
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code StartEndTimeConsumer} 为起止时间消费者接口，扩展 {@code CBiConsumer&lt;Instant, Instant&gt;}，专门消费起止时间：</p>
 * <ul>
 *   <li>{@code acceptThrowable(Instant startTime, Instant endTime)}：可抛受检异常</li>
 * </ul>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>消费起止时间范围（如分页时间区间处理）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>为 CBiConsumer&lt;Instant, Instant&gt; 的类型化别名。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义</b></p>
 * <ul>
 *   <li>固定参数为起止时间（Instant），复用 CBiConsumer 的受检异常包装与工具方法，重声明 acceptThrowable。</li>
 * </ul>
 *
 * @since 2025/10/31
 * @version 1.0
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
