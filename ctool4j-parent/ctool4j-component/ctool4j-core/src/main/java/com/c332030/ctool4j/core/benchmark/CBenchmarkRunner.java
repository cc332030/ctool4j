package com.c332030.ctool4j.core.benchmark;

import lombok.CustomLog;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * <p>
 * Description: 轻量性能基准运行器（通用框架）
 * </p>
 * <p>
 * 架构说明：本框架置于 core 主代码，各模块性能基准用例放各模块测试源码目录
 * （JUnit 性能测试类命名以 PerfTests 结尾，由根 pom.xml 的排除规则挡在常规测试之外），
 * 由测试方法调用 {@link #run(List, String)} 触发基准，返回报告后可写入文件。
 * </p>
 * <p>
 * 流程：先对所有用例做一轮预热，触发全部实现方式初始化/加载（初始化干扰不计入结果）；
 * 随后对每个用例进行多轮采样，每轮先充分预热再正式计时，
 * run 返回值经 identityHashCode 累计，防止 JIT 将无副作用的循环体消除。
 * 最终各用例取多轮平均值作为结果，降低单次测量噪声。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CBenchmarkRunner} 为轻量性能基准运行器，提供 {@code run(List&lt;CBenchmarkCase&gt;, String title)}：</p>
 * <ul>
 *   <li>预热：{@code WARMUP_ITERATIONS=500000} 次（触发 JIT 编译至 C2 稳态）</li>
 *   <li>计时：{@code MEASURE_ITERATIONS=1000000} 次/轮，{@code MEASURE_ROUNDS=5} 轮采样取平均</li>
 *   <li>返回 {@code CBenchmarkReport}（并通过日志输出报告）</li>
 * </ul>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>各模块性能基准用例的执行（基准用例放测试源码目录，由测试方法触发）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>轻量级基准，非 JMH 级精度；用于横向对比实现方式耗时。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>执行流程</b></p>
 * <ul>
 *   <li>第一轮对所有用例预热，触发全部实现方式初始化/加载（初始化干扰不计入结果）。</li>
 *   <li>第二轮对每个用例做多轮采样（默认 5 轮）：每轮开始前先 {@code prepare()} 并充分预热，再正式计时；</li>
 *   <li>多轮耗时取平均作为该用例结果，降低 JIT/GC 调度噪声。</li>
 *   <li>纳秒级操作（如 MethodHandle）需足够迭代才能稳定，故预热迭代提升至 50 万、单轮计时迭代 100 万。</li>
 * </ul>
 * <p><b>结果排序</b></p>
 * <ul>
 *   <li>结果按 {@code avgNanos} 升序排序，首项为基线。</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.0
 */
@CustomLog
public class CBenchmarkRunner {

    /**
     * 预热次数（不计时，触发 JIT 编译；纳秒级操作需足够迭代才能到达 C2 稳态）
     */
    private static final int WARMUP_ITERATIONS = 500_000;

    /**
     * 单轮计时迭代次数
     */
    private static final int MEASURE_ITERATIONS = 1_000_000;

    /**
     * 采样轮数（多轮取平均，降低 JIT/GC 调度噪声）
     */
    private static final int MEASURE_ROUNDS = 5;

    /**
     * 运行一组基准用例（预热 + 多轮计时取均值），返回报告（并输出日志报告）
     * <ul>
     *   <li>{@code run()} 返回值经 {@code System.identityHashCode} 累计到 blackhole，防止 JIT 将无副作用的循环体消除。</li>
     * </ul>
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

        // 第二轮：对每个用例做多轮采样（每轮先预热再计时），取平均
        List<CBenchmarkResult> results = new ArrayList<>();

        for (CBenchmarkCase bc : cases) {

            long totalNanos = 0;

            for (int round = 0; round < MEASURE_ROUNDS; round++) {

                bc.prepare();

                // 每轮开始前充分预热，保证测量在稳态下进行
                for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                    bc.run();
                }

                long blackhole = 0;
                long start = System.nanoTime();
                for (int i = 0; i < MEASURE_ITERATIONS; i++) {
                    blackhole += System.identityHashCode(bc.run());
                }
                long elapsed = System.nanoTime() - start;

                totalNanos += elapsed;

                // 防止 JIT 消除，blackhole 仅参与一次无副作用累加
                if (blackhole == Long.MIN_VALUE) {
                    log.debug("unreachable");
                }
            }

            // 累计全部迭代的耗时与迭代次数，得到精确的平均耗时
            results.add(CBenchmarkResult.builder()
                .name(bc.name())
                .iterations((long) MEASURE_ITERATIONS * MEASURE_ROUNDS)
                .elapsedNanos(totalNanos)
                .build());
        }

        results.sort(Comparator.comparingDouble(CBenchmarkResult::avgNanos));

        CBenchmarkReport report = CBenchmarkReport.builder()
            .title(title)
            .results(results)
            .build();

        print(report);
        return report;
    }

    private static void print(CBenchmarkReport report) {

        double baseline = report.getResults().get(0).avgNanos();

        log.info(String.format("%-24s %16s %16s %14s", "实现方式", "Avg(ns/op)", "ops/s", "相对基线"));
        log.info("--------------------------------------------------------------------------");
        for (CBenchmarkResult result : report.getResults()) {
            log.info(String.format("%-24s %16.1f %16.0f %12.2fx",
                    result.getName(),
                    result.avgNanos(),
                    result.opsPerSecond(),
                    result.avgNanos() / baseline
            ));
        }
    }

}
