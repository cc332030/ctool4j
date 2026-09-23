package com.c332030.ctool4j.core.benchmark;

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
 *   <li>2.4 边界：基线均值为 0 → 相对基线与吞吐输出"不可比"文字，不出现 {@code NaN}/{@code Infinity}</li>
 * </ul>
 * <h2>writeTo(Path)</h2>
 * <ul>
 *   <li>3.1 正例：写入临时文件成功，内容与 {@code toMarkdown()} 一致，父目录自动创建</li>
 *   <li>3.2 边界：无父目录的相对路径也可写入成功</li>
 *   <li>3.3 异常：目标路径为已存在目录 → 抛 RuntimeException（IO 失败）</li>
 * </ul>
 *
 * @since 2026/8/21
 * @version 1.1
 */
public class CBenchmarkReportTests {

    /**
     * 构造一个含两条结果（首项为基线）的报告
     *
     * @return 报告
     */
    private static CBenchmarkReport reportWithTwoResults() {

        List<CBenchmarkResult> results = Arrays.asList(
            CBenchmarkResult.builder().name("base").iterations(1000).elapsedNanos(1_000_000_000)
                    .roundAvgNanos(new double[] {1_000_000.0, 1_000_100.0, 1_000_200.0}).build(),
            CBenchmarkResult.builder().name("fast").iterations(1000).elapsedNanos(500_000_000)
                    .roundAvgNanos(new double[] {500_000.0, 500_050.0, 500_100.0}).build()
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

        List<CBenchmarkResult> results = Collections.singletonList(
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
        // 终值与离散度同表同格（缺离散度的成绩不成立）
        Assertions.assertTrue(
                markdown.contains("| 实现方式 | Avg(ns/op) | 离散度(极差 ns/op) | 相对离散度 | ops/s | 相对基线 | 结论 |"),
                "表头应含离散度与结论列"
        );
        // 基线行：均值 1000100.0、极差 200.0、结论固定为「基线」
        Assertions.assertTrue(markdown.contains("| base | 1000100.0 | 200.0 | "), "基线行应含终值与极差");
        Assertions.assertTrue(markdown.contains("| 1.00x | 基线 |"), "基线行结论应为「基线」");
        // 快 50% 且差异远超两者离散度 → 判为「优于基线」
        Assertions.assertTrue(markdown.contains("| fast | 500050.0 | 100.0 | "), "fast 行应含终值与极差");
        Assertions.assertTrue(markdown.contains("| 0.50x | 优于基线 |"), "差异远超离散度应判为优于基线");

    }

    /**
     * 对应测试用例 2.2：边界：单结果，相对基线为 1.00x
     */
    @Test
    public void toMarkdownWithSingleResult() {

        List<CBenchmarkResult> results = Collections.singletonList(
            CBenchmarkResult.builder().name("only").iterations(1000).elapsedNanos(1_000_000_000)
                    .roundAvgNanos(new double[] {1_000_000.0, 1_000_000.0, 1_000_000.0}).build()
        );

        CBenchmarkReport report = CBenchmarkReport.builder()
            .title("benchmark")
            .results(results)
            .build();

        String markdown = report.toMarkdown();

        Assertions.assertTrue(markdown.contains("| only | 1000000.0 | 0.0 | 0.0% | 1000 | 1.00x | 基线 |"), "单结果应给出终值/离散度与基线结论");

    }

    /**
     * 对应测试用例 2.3：异常：空结果列表 → 抛 IndexOutOfBoundsException
     */
    @Test
    public void toMarkdownWithEmptyResults() {

        CBenchmarkReport report = CBenchmarkReport.builder()
            .title("benchmark")
            .results(Collections.emptyList())
            .build();

        Assertions.assertThrowsExactly(
            IndexOutOfBoundsException.class,
            report::toMarkdown
        );

    }

    /**
     * 测试基线均值为 0 时的取数：比值与吞吐按"不可比"输出文字
     * 对应测试用例 2.4：基线均值为 0 → 不出现 NaN/Infinity
     *
     * <p><b>回归点</b>：基线均值为 0 时 {@code avgNanos / baselineAvg} 得 {@code NaN} 或
     * {@code Infinity}，格式化后渲染成 {@code NaNx}/{@code Infinityx}，Ops 列也会输出 {@code Infinity}——
     * 性能结论里出现 {@code NaN}/{@code Infinity} 会被误读为"极快/极慢"，而实际是"基线无有效耗时、
     * 比值无意义"。现按"不可比"输出文字，数字列不再出现非有限值。</p>
     *
     * <p>断言口径：①基线行（均值为 0）与其余行的「相对基线」列均为"不可比（基线为 0）"；
     * ②基线行「ops/s」列为"-（均值为 0）"；③整份报告不含 {@code NaN} 与 {@code Infinity} 文本。</p>
     */
    @Test
    public void toMarkdownWithZeroBaseline() {

        List<CBenchmarkResult> results = Arrays.asList(
            CBenchmarkResult.builder().name("zero").iterations(1000).elapsedNanos(0).build(),
            CBenchmarkResult.builder().name("other").iterations(1000).elapsedNanos(5).build()
        );

        CBenchmarkReport report = CBenchmarkReport.builder()
            .title("zeroBaseline")
            .results(results)
            .build();

        String markdown = report.toMarkdown();

        Assertions.assertTrue(markdown.contains("| 不可比（基线为 0） | 基线 |"), "基线行相对基线应为不可比");
        Assertions.assertTrue(markdown.contains("| -（均值为 0） | 不可比（基线为 0） |"), "零均值行吞吐应为不可比");
        Assertions.assertFalse(markdown.contains("NaN"), "零基线不应渲染出 NaN");
        Assertions.assertFalse(markdown.contains("Infinity"), "零基线不应渲染出 Infinity");
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
