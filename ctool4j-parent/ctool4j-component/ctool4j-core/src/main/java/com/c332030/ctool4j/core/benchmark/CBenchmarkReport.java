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
 *   <li>表格含「实现方式 / Avg(ns/op) / ops/s / 相对基线」列，相对基线为各结果与基线（首项）耗时比。</li>
 * </ul>
 * <p><b>文件写出</b></p>
 * <ul>
 *   <li>{@code writeTo} 自动创建父目录并以 UTF-8 写出，IO 异常包装为 RuntimeException。</li>
 * </ul>
 *
 * @since 2026/8/20
 * @version 1.0
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
        sb.append("| 实现方式 | Avg(ns/op) | ops/s | 相对基线 |\n");
        sb.append("| --- | ---: | ---: | ---: |\n");
        for (CBenchmarkResult result : results) {
            sb.append("| ").append(result.getName())
                .append(" | ").append(String.format("%.1f", result.avgNanos()))
                .append(" | ").append(String.format("%.0f", result.opsPerSecond()))
                .append(" | ").append(String.format("%.2fx", result.avgNanos() / baseline.avgNanos()))
                .append(" |\n");
        }
        sb.append("\n");
        return sb.toString();
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
