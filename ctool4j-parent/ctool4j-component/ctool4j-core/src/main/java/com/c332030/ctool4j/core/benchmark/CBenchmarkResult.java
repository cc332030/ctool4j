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
 *   <li>{@code avgNanos = 各轮单次均摊耗时的均值}（无逐轮明细时退化为 {@code elapsedNanos / iterations}）</li>
 *   <li>{@code opsPerSecond = 1e9 / avgNanos}</li>
 *   <li>{@code dispersionNanos = 各轮单次均摊耗时的极差（最慢 − 最快）}、{@code dispersionRatio = 极差 / 均值}</li>
 * </ul>
 * <p><b>样本量与离散度</b></p>
 * <ul>
 *   <li>采样轮数 ≥ 3（由 {@code CBenchmarkRunner} 保证）；离散度与均值同表给出，
 *   缺离散度的成绩不成立（差异小于离散度即视为无显著差异）。</li>
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
     * 各轮次单次均摊耗时（纳秒/次，长度 = 采样轮数 ≥ 3）：离散度的取值来源
     *
     * <p>只保留"每轮聚合后的单次均摊值"这一层聚合结果，不保留逐轮原始纳秒总量——
     * 轮数不同、迭代数不同时无从比较；单次均摊值使各轮可直接对比、离散度可直接计算。</p>
     */
    private final double[] roundAvgNanos;

    /**
     * 平均耗时（纳秒/次，各轮单次均摊耗时的均值）
     *
     * @return 平均耗时（纳秒/次）
     */
    public double avgNanos() {
        if(null != roundAvgNanos && roundAvgNanos.length > 0) {
            double sum = 0;
            for (double round : roundAvgNanos) {
                sum += round;
            }
            return sum / roundAvgNanos.length;
        }
        return elapsedNanos * 1.0 / iterations;
    }

    /**
     * 每秒执行次数
     *
     * @return 每秒执行次数
     */
    public double opsPerSecond() {
        double avg = avgNanos();
        return avg <= 0 ? Double.POSITIVE_INFINITY : 1_000_000_000.0 / avg;
    }

    /**
     * 离散度：各轮单次均摊耗时的极差（最慢 − 最快）
     *
     * <p>取极差而非标准差：性能测试规范要求"组间差异小于离散度即视为无显著差异"，
     * 极差是这一判据的最直接口径（也避免轮数少时标准差的偏差）。</p>
     *
     * @return 极差（纳秒/次）；不足 2 轮时返回 0
     */
    public double dispersionNanos() {
        if(null == roundAvgNanos || roundAvgNanos.length < 2) {
            return 0;
        }
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (double round : roundAvgNanos) {
            min = Math.min(min, round);
            max = Math.max(max, round);
        }
        return max - min;
    }

    /**
     * 离散度相对值：极差 / 均值（用于跨用例比较测量稳定性）
     *
     * @return 相对离散度；均值为 0 时返回 0
     */
    public double dispersionRatio() {
        double avg = avgNanos();
        return avg <= 0 ? 0 : dispersionNanos() / avg;
    }

}
