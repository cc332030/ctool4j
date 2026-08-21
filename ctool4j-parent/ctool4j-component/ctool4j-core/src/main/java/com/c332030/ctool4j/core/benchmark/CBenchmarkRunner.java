package com.c332030.ctool4j.core.benchmark;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * <p>
 * Description: 轻量性能基准运行器（通用框架）
 * </p>
 * <p>
 * 架构说明：本框架置于 core 主代码，各模块性能基准用例放各模块测试源码目录
 * （JUnit 测试类命名以 BenchmarkTests 结尾，surefire 打包/测试时排除），
 * 由测试方法调用 {@link #run(List, String)} 触发基准，返回报告后可写入文件。
 * </p>
 * <p>
 * 流程：共测试两次。第一轮对所有用例预热，触发全部实现方式初始化/加载
 * （初始化干扰不计入结果）；第二轮在全部初始化完成后再预热并正式计时，
 * run 返回值经 identityHashCode 累计，防止 JIT 将无副作用的循环体消除。
 * </p>
 *
 * @since 2026/8/16
 * @see "doc/design/core/CBenchmarkRunner.adoc"
 */
public class CBenchmarkRunner {

    /**
     * 预热次数（不计时，触发 JIT 编译）
     */
    private static final int WARMUP_ITERATIONS = 50_000;

    /**
     * 计时迭代次数
     */
    private static final int MEASURE_ITERATIONS = 100_000;

    /**
     * 运行一组基准用例（预热 + 计时），返回报告（含控制台打印）
     *
     * @param cases 基准用例列表
     * @param title 报告标题
     * @return 基准报告（可用于导出 markdown 文件）
     */
    public static CBenchmarkReport run(List<CBenchmarkCase> cases, String title) {

        // 第一轮：对所有用例预热，触发全部实现方式初始化/加载（结果不计入）
        for (CBenchmarkCase bc : cases) {

            bc.prepare();

            for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                bc.run();
            }
        }

        // 第二轮：全部初始化完成后，再预热并正式计时
        List<CBenchmarkResult> results = new ArrayList<>();

        for (CBenchmarkCase bc : cases) {

            bc.prepare();

            for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                bc.run();
            }

            long blackhole = 0;
            long start = System.nanoTime();
            for (int i = 0; i < MEASURE_ITERATIONS; i++) {
                blackhole += System.identityHashCode(bc.run());
            }
            long elapsed = System.nanoTime() - start;

            results.add(new CBenchmarkResult(bc.name(), MEASURE_ITERATIONS, elapsed));

            // 防止 JIT 消除，blackhole 仅参与一次无副作用累加
            if (blackhole == Long.MIN_VALUE) {
                System.out.println("unreachable");
            }
        }

        results.sort(Comparator.comparingDouble(CBenchmarkResult::avgNanos));

        CBenchmarkReport report = new CBenchmarkReport(title, results);

        print(report);
        return report;
    }

    private static void print(CBenchmarkReport report) {

        double baseline = report.getResults().get(0).avgNanos();

        System.out.println();
        System.out.println("===== " + report.getTitle() + "（" + MEASURE_ITERATIONS + " 次迭代）=====");
        System.out.printf("%-24s %16s %16s %14s%n", "实现方式", "Avg(ns/op)", "ops/s", "相对基线");
        System.out.println("--------------------------------------------------------------------------");
        for (CBenchmarkResult result : report.getResults()) {
            System.out.printf("%-24s %16.1f %16.0f %12.2fx%n",
                    result.getName(),
                    result.avgNanos(),
                    result.opsPerSecond(),
                    result.avgNanos() / baseline
            );
        }
        System.out.println();
    }

}
