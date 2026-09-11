package com.c332030.ctool4j.core.test.benchmark;

import com.c332030.ctool4j.core.benchmark.CBenchmarkReport;
import com.c332030.ctool4j.core.benchmark.CBenchmarkResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Description: CBenchmarkReportTests
 * </p>
 *
 * <p>
 * 是 {@link CBenchmarkReport} 的测试用例
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>白盒分析：{@code toMarkdown()} 遍历结果列表，取首项为基线计算"相对基线"（{@code avgNanos / baseline}）；</li>
 *   <li>{@code writeTo(Path)} 先创建父目录再以 UTF-8 写出，IO 异常包装为 {@code RuntimeException}。需覆盖</li>
 *   <li>多结果表格、单结果（相对基线 1.00x）、空列表（{@code results.get(0)} 抛</li>
 *   <li>{@code IndexOutOfBoundsException}）、文件写入成功与失败分支。</li>
 *   <li>黑盒分析：入参为标题与结果列表，出参为 markdown 字符串与写入的文件；取值覆盖</li>
 *   <li>典型多结果、单结果、空列表（异常）、相对路径父目录创建。</li>
 *   <li>错误推测法：空列表调用 {@code toMarkdown}/{@code writeTo} 会在 {@code results.get(0)} 处抛</li>
 *   <li>{@code IndexOutOfBoundsException}；{@code writeTo} 对无父目录的简单路径不创建目录；写入失败（如</li>
 *   <li>目标路径为已存在目录）抛 {@code RuntimeException}。</li>
 * </ul>
 * <h2>覆盖场景</h2>
 * <ul>
 *   <li>覆盖：{@code getTitle}/{@code getResults} 正例；{@code toMarkdown} 多结果表格列完整、单结果基线为 1.00x、</li>
 *   <li>空列表异常；{@code writeTo} 写入成功（含自动创建父目录）、UTF-8 编码、目标为目录时抛</li>
 *   <li>{@code RuntimeException}。</li>
 *   <li>未覆盖：{@code String.format} 的 locale 差异（CI 与本地均按默认 locale 运行，非跨 locale 场景）。</li>
 * </ul>
 * <h2>字段取值（getTitle / getResults）</h2>
 * <ul>
 *   <li>1.1 正例：构造后 {@code getTitle} / {@code getResults} 原样返回构造入参</li>
 * </ul>
 * <h2>toMarkdown()</h2>
 * <ul>
 *   <li>2.1 正例：多结果，表格含「实现方式 / Avg(ns/op) / ops/s / 相对基线」各列，相对基线以首项为基线</li>
 *   <li>2.2 边界：单结果，相对基线为 1.00x</li>
 *   <li>2.3 异常：空结果列表 → 抛 IndexOutOfBoundsException</li>
 * </ul>
 * <h2>writeTo(Path)</h2>
 * <ul>
 *   <li>3.1 正例：写入临时文件成功，内容与 {@code toMarkdown()} 一致，父目录自动创建</li>
 *   <li>3.2 边界：无父目录的相对路径也可写入成功</li>
 *   <li>3.3 异常：目标路径为已存在目录 → 抛 RuntimeException（IO 失败）</li>
 * </ul>
 *
 * @since 2026/8/21
 * @version 1.0
 */
public class CBenchmarkReportTests {

    /**
     * 构造一个含两条结果（首项为基线）的报告
     *
     * @return 报告
     */
    private static CBenchmarkReport reportWithTwoResults() {

        List<CBenchmarkResult> results = Arrays.asList(
            CBenchmarkResult.builder().name("base").iterations(1000).elapsedNanos(1_000_000_000).build(),
            CBenchmarkResult.builder().name("fast").iterations(1000).elapsedNanos(500_000_000).build()
        );

        return CBenchmarkReport.builder()
            .title("benchmark")
            .results(results)
            .build();

    }

    /**
     * 对应测试用例 1.1：正例：构造后 {@code getTitle} / {@code getResults} 原样返回构造入参
     */
    @Test
    public void fieldsReturnedAsIs() {

        List<CBenchmarkResult> results = Arrays.asList(
            CBenchmarkResult.builder().name("base").iterations(1000).elapsedNanos(1000).build()
        );

        CBenchmarkReport report = CBenchmarkReport.builder()
            .title("title")
            .results(results)
            .build();

        Assertions.assertEquals("title", report.getTitle());
        Assertions.assertSame(results, report.getResults());

    }

    /**
     * 对应测试用例 2.1：正例：多结果，表格含「实现方式 / Avg(ns/op) / ops/s / 相对基线」各列，相对基线以首项为基线
     */
    @Test
    public void toMarkdownWithMultipleResults() {

        CBenchmarkReport report = reportWithTwoResults();

        String markdown = report.toMarkdown();

        Assertions.assertTrue(markdown.startsWith("# benchmark\n\n"));
        Assertions.assertTrue(markdown.contains("| 实现方式 | Avg(ns/op) | ops/s | 相对基线 |"));
        Assertions.assertTrue(markdown.contains("| base | 1000000.0 | 1000 | 1.00x |"));
        Assertions.assertTrue(markdown.contains("| fast | 500000.0 | 2000 | 0.50x |"));

    }

    /**
     * 对应测试用例 2.2：边界：单结果，相对基线为 1.00x
     */
    @Test
    public void toMarkdownWithSingleResult() {

        List<CBenchmarkResult> results = Arrays.asList(
            CBenchmarkResult.builder().name("only").iterations(1000).elapsedNanos(1_000_000_000).build()
        );

        CBenchmarkReport report = CBenchmarkReport.builder()
            .title("benchmark")
            .results(results)
            .build();

        String markdown = report.toMarkdown();

        Assertions.assertTrue(markdown.contains("| only | 1000000.0 | 1000 | 1.00x |"));

    }

    /**
     * 对应测试用例 2.3：异常：空结果列表 → 抛 IndexOutOfBoundsException
     */
    @Test
    public void toMarkdownWithEmptyResults() {

        CBenchmarkReport report = CBenchmarkReport.builder()
            .title("benchmark")
            .results(Collections.<CBenchmarkResult>emptyList())
            .build();

        Assertions.assertThrowsExactly(
            IndexOutOfBoundsException.class,
            report::toMarkdown
        );

    }

    /**
     * 对应测试用例 3.1：正例：写入临时文件成功，内容与 {@code toMarkdown()} 一致，父目录自动创建
     */
    @Test
    public void writeToCreatesParentDir(@TempDir Path tempDir) throws IOException {

        CBenchmarkReport report = reportWithTwoResults();

        Path target = tempDir.resolve("sub").resolve("report.md");
        report.writeTo(target);

        Assertions.assertTrue(Files.exists(target));
        String content = new String(Files.readAllBytes(target), StandardCharsets.UTF_8);
        Assertions.assertEquals(report.toMarkdown(), content);

    }

    /**
     * 对应测试用例 3.2：边界：无父目录的相对路径也可写入成功
     */
    @Test
    public void writeToSimpleRelativePath(@TempDir Path tempDir) {

        CBenchmarkReport report = reportWithTwoResults();

        // 无父目录的相对路径
        Path target = Paths.get(tempDir.toString(), "report.md");
        report.writeTo(target);

        Assertions.assertTrue(Files.exists(target));

    }

    /**
     * 对应测试用例 3.3：异常：目标路径为已存在目录 → 抛 RuntimeException（IO 失败）
     */
    @Test
    public void writeToDirectoryThrows(@TempDir Path tempDir) {

        CBenchmarkReport report = reportWithTwoResults();

        // 目标路径为已存在目录，写入失败应包装为 RuntimeException
        Assertions.assertThrowsExactly(RuntimeException.class, () -> report.writeTo(tempDir));

    }

}
