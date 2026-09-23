package com.c332030.ctool4j.core.benchmark;

import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * <p>
 * Description: 性能基准报告（含标题与各用例结果），支持导出 markdown 文件
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CBenchmarkReport} 为性能基准报告，含标题与按耗时升序的结果列表（首项为基线），提供：</p>
 * <ul>
 *   <li>{@code getTitle()} / {@code getResults()}</li>
 * </ul>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>汇总基准结果并导出 markdown 报告文件。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>相对基线假设 results 按耗时升序（首项为基线），由 CBenchmarkRunner 保证。</li>
 * </ul>
 * <p>。</p>
 * <h2>设计要点</h2>
 * <p><b>markdown 导出</b></p>
 * <ul>
 *   <li>表格含「实现方式 / Avg(ns/op) / 离散度(极差) / 相对离散度 / ops/s / 相对基线 / 结论」列：
 *   终值与离散度同表同格（缺离散度的成绩不成立），相对基线为各结果与基线（首项）耗时比，
 *   结论按「差异是否超过两者离散度之和」判定（未超过即标"无显著差异"）。</li>
 *   <li>基线均值为 0 时比值与吞吐无意义（{@code x/0} 会渲染成 {@code NaNx}/{@code Infinityx}），
 *   按"不可比"输出文字而非数字（见 {@code formatRatio} / {@code formatOpsPerSecond}）。</li>
 * </ul>
 * <p><b>文件写出</b></p>
 * <ul>
 *   <li>{@code writeTo} 自动创建父目录并以 UTF-8 写出，IO 异常包装为 RuntimeException。</li>
 * </ul>
 *
 * @since 2026/8/20
 * @version 1.1
 */
@SuperBuilder
@RequiredArgsConstructor
public class CBenchmarkReport {

    /**
     * 报告标题
     */
    private final String title;

    /**
     * 按耗时升序的结果列表（首项为基线）
     */
    private final List<CBenchmarkResult> results;

    /**
     * 获取报告标题
     *
     * @return 标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取结果列表（按耗时升序，首项为基线）
     *
     * @return 结果列表
     */
    public List<CBenchmarkResult> getResults() {
        return results;
    }

    /**
     * 导出为 markdown 表格
     * <ul>
     *   <li>{@code toMarkdown()}：导出为 markdown 表格</li>
     * </ul>
     *
     * @return markdown 内容
     */
    public String toMarkdown() {

        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(title).append("\n\n");

        CBenchmarkResult baseline = results.get(0);
        double baselineAvg = baseline.avgNanos();
        // 终值与离散度同表同格给出（缺离散度的成绩不成立）：终值 + 极差 + 相对离散度
        sb.append("| 实现方式 | Avg(ns/op) | 离散度(极差 ns/op) | 相对离散度 | ops/s | 相对基线 | 结论 |\n");
        sb.append("| --- | ---: | ---: | ---: | ---: | ---: | --- |\n");
        for (CBenchmarkResult result : results) {
            sb.append("| ").append(result.getName())
                .append(" | ").append(String.format("%.1f", result.avgNanos()))
                .append(" | ").append(String.format("%.1f", result.dispersionNanos()))
                .append(" | ").append(String.format("%.1f%%", result.dispersionRatio() * 100))
                .append(" | ").append(formatOpsPerSecond(result))
                .append(" | ").append(formatRatio(result.avgNanos(), baselineAvg))
                .append(" | ").append(result == baseline ? "基线" : conclusionOf(result, baseline))
                .append(" |\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * 相对基线的格式化：基线均值为 0 时比值无意义（{@code x/0} 会渲染成 {@code NaNx}/{@code Infinityx}），
     * 按"不可比"输出文字而非数字
     *
     * @param value        待比较的耗时
     * @param baselineAvg  基线均值
     * @return 比值文本（如 {@code 0.50x}）；基线不可比时返回 {@code 不可比（基线为 0）}
     */
    private static String formatRatio(double value, double baselineAvg) {

        if(baselineAvg <= 0) {
            return "不可比（基线为 0）";
        }

        return String.format("%.2fx", value / baselineAvg);
    }

    /**
     * 吞吐的格式化：均值为 0（无有效耗时）时吞吐为无穷，按"不可比"输出文字而非 {@code Infinity}
     *
     * @param result 基准结果
     * @return 吞吐文本（如 {@code 1000}）；均值为 0 时返回 {@code -（均值为 0）}
     */
    private static String formatOpsPerSecond(CBenchmarkResult result) {

        double ops = result.opsPerSecond();
        if(Double.isInfinite(ops) || Double.isNaN(ops)) {
            return "-（均值为 0）";
        }

        return String.format("%.0f", ops);
    }

    /**
     * 结论判定：组间差异小于两者离散度之和即视为「无显著差异」（不得据此宣称胜出）
     *
     * @param result   待判定结果
     * @param baseline 基线结果
     * @return 结论文本
     */
    private static String conclusionOf(CBenchmarkResult result, CBenchmarkResult baseline) {

        double delta = result.avgNanos() - baseline.avgNanos();
        double noise = result.dispersionNanos() + baseline.dispersionNanos();
        if(Math.abs(delta) <= noise) {
            return "无显著差异（差异 < 离散度）";
        }

        return delta < 0 ? "优于基线" : "劣于基线";
    }

    /**
     * 将报告写为 markdown 文件（自动创建父目录，UTF-8）
     * <ul>
     *   <li>{@code writeTo(Path)}：写为 markdown 文件（自动创建父目录，UTF-8）</li>
     * </ul>
     *
     * @param path 目标文件路径
     */
    public void writeTo(Path path) {
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(path, toMarkdown().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("写入性能测试报告失败: " + path, e);
        }
    }

}
