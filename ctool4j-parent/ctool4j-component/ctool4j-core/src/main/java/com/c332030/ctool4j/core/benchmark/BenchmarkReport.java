package com.c332030.ctool4j.core.benchmark;

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
 * @since 2026/8/20
 */
public class BenchmarkReport {

    /**
     * 报告标题
     */
    private final String title;

    /**
     * 按耗时升序的结果列表（首项为基线）
     */
    private final List<BenchmarkResult> results;

    /**
     * 构造报告
     *
     * @param title   标题
     * @param results 结果列表（按耗时升序）
     */
    public BenchmarkReport(String title, List<BenchmarkResult> results) {
        this.title = title;
        this.results = results;
    }

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
    public List<BenchmarkResult> getResults() {
        return results;
    }

    /**
     * 导出为 markdown 表格
     *
     * @return markdown 内容
     */
    public String toMarkdown() {

        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(title).append("\n\n");

        BenchmarkResult baseline = results.get(0);
        sb.append("| 实现方式 | Avg(ns/op) | ops/s | 相对基线 |\n");
        sb.append("| --- | ---: | ---: | ---: |\n");
        for (BenchmarkResult result : results) {
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
