package com.c332030.ctool4j.core.benchmark;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: 基准执行结果
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CBenchmarkResult} 为基准执行结果，含用例名、迭代次数、总耗时（纳秒），提供：</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>保存并计算单个基准用例的执行指标，供 {@code CBenchmarkReport} 汇总展示。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>通过 @RequiredArgsConstructor 生成全参构造，字段只读（@Getter）。</li>
 * </ul>
 * <p>。</p>
 * <h2>设计要点</h2>
 * <p><b>指标计算</b></p>
 * <ul>
 *   <li>{@code avgNanos = elapsedNanos / iterations}</li>
 *   <li>{@code opsPerSecond = iterations / (elapsedNanos / 1e9)}</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.0
 */
@Getter
@SuperBuilder
@RequiredArgsConstructor
public class CBenchmarkResult {

    private final String name;

    private final long iterations;

    private final long elapsedNanos;

    /**
     * 平均耗时（纳秒/次）
     * <ul>
     *   <li>{@code avgNanos()}：平均耗时（纳秒/次）</li>
     * </ul>
     *
     * @return 平均耗时（纳秒/次）
     */
    public double avgNanos() {
        return elapsedNanos * 1.0 / iterations;
    }

    /**
     * 每秒执行次数
     * <ul>
     *   <li>{@code opsPerSecond()}：每秒执行次数</li>
     * </ul>
     *
     * @return 每秒执行次数
     */
    public double opsPerSecond() {
        return iterations * 1.0 / (elapsedNanos / 1_000_000_000.0);
    }

}
