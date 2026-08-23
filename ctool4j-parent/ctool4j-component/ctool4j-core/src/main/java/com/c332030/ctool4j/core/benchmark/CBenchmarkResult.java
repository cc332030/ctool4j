package com.c332030.ctool4j.core.benchmark;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * Description: 基准执行结果
 * </p>
 *
 * @since 2026/8/16
 * @see "doc/design/core/CBenchmarkResult.adoc"
 * @see "doc/design/core/CBenchmarkResultTests.adoc"
 */
@Getter
@RequiredArgsConstructor
public class CBenchmarkResult {

    private final String name;

    private final long iterations;

    private final long elapsedNanos;

    /**
     * 平均耗时（纳秒/次）
     */
    public double avgNanos() {
        return elapsedNanos * 1.0 / iterations;
    }

    /**
     * 每秒执行次数
     */
    public double opsPerSecond() {
        return iterations * 1.0 / (elapsedNanos / 1_000_000_000.0);
    }

}
