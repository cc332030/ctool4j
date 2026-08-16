package com.c332030.ctool4j.core.benchmark;

/**
 * <p>
 * Description: 基准执行结果
 * </p>
 *
 * @since 2026/8/16
 */
public class BenchmarkResult {

    private final String name;

    private final long iterations;

    private final long elapsedNanos;

    public BenchmarkResult(String name, long iterations, long elapsedNanos) {
        this.name = name;
        this.iterations = iterations;
        this.elapsedNanos = elapsedNanos;
    }

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

    public String getName() {
        return name;
    }

    public long getIterations() {
        return iterations;
    }

    public long getElapsedNanos() {
        return elapsedNanos;
    }

}
