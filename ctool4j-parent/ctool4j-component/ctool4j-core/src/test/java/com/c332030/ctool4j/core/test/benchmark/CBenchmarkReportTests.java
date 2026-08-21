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
 * @since 2026/8/21
 */
public class CBenchmarkReportTests {

    /**
     * 构造一个含两条结果（首项为基线）的报告
     *
     * @return 报告
     */
    private static CBenchmarkReport reportWithTwoResults() {

        List<CBenchmarkResult> results = Arrays.asList(
            new CBenchmarkResult("base", 1000, 1_000_000_000),
            new CBenchmarkResult("fast", 1000, 500_000_000)
        );

        return new CBenchmarkReport("benchmark", results);

    }

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void fieldsReturnedAsIs() {

        List<CBenchmarkResult> results = Arrays.asList(
            new CBenchmarkResult("base", 1000, 1000)
        );

        CBenchmarkReport report = new CBenchmarkReport("title", results);

        Assertions.assertEquals("title", report.getTitle());
        Assertions.assertSame(results, report.getResults());

    }

    /**
     * 对应测试用例 2.1
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
     * 对应测试用例 2.2
     */
    @Test
    public void toMarkdownWithSingleResult() {

        List<CBenchmarkResult> results = Arrays.asList(
            new CBenchmarkResult("only", 1000, 1_000_000_000)
        );

        CBenchmarkReport report = new CBenchmarkReport("benchmark", results);

        String markdown = report.toMarkdown();

        Assertions.assertTrue(markdown.contains("| only | 1000000.0 | 1000 | 1.00x |"));

    }

    /**
     * 对应测试用例 2.3
     */
    @Test
    public void toMarkdownWithEmptyResults() {

        CBenchmarkReport report = new CBenchmarkReport("benchmark", Collections.emptyList());

        Assertions.assertThrowsExactly(
            IndexOutOfBoundsException.class,
            report::toMarkdown
        );

    }

    /**
     * 对应测试用例 3.1
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
     * 对应测试用例 3.2
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
     * 对应测试用例 3.3
     */
    @Test
    public void writeToDirectoryThrows(@TempDir Path tempDir) {

        CBenchmarkReport report = reportWithTwoResults();

        // 目标路径为已存在目录，写入失败应包装为 RuntimeException
        Assertions.assertThrowsExactly(RuntimeException.class, () -> report.writeTo(tempDir));

    }

}
