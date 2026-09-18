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
 *   <li>计时：{@code MEASURE_ITERATIONS=1000000} 次/轮，{@code MEASURE_ROUNDS=5} 轮采样（≥3 次有效采样）</li>
 *   <li>测量口径：终值为各轮「单次均摊耗时」的均值，离散度为极差（最慢 − 最快）；两者一并输出</li>
 *   <li>返回 {@code CBenchmarkReport}（并通过日志输出报告）</li>
 * </ul>
 * <h2>测量口径（可核对）</h2>
 * <ul>
 *   <li>预热轮次：第一轮全用例预热（触发初始化/加载）+ 每轮计时前再预热，均不计时；</li>
 *   <li>测量轮次 / 批量：{@link #MEASURE_ROUNDS} 轮 × {@link #MEASURE_ITERATIONS} 次；
 *   每轮计时前先预热并归集一次垃圾（各轮同状态起步，抵消轮间 GC 漂移）；</li>
 *   <li>计时区间：只含 {@code run()} 调用循环——{@code prepare()}（数据构造）在区间外，
 *   报告写出也在区间外；</li>
 *   <li>指标：耗时（ns/op）+ 离散度（极差，ns/op）+ 吞吐（ops/s）；</li>
 *   <li>结果消费：{@code run()} 返回值经 {@code identityHashCode} 累计进 blackhole，防 JIT 消除死代码。</li>
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
        return run(cases, title, WARMUP_ITERATIONS, MEASURE_ITERATIONS, MEASURE_ROUNDS);
    }

    /**
     * 运行一组基准用例（预热 + 多轮计时取均值），可指定迭代参数
     *
     * <p>不同通道的用例耗时量级差异大（如深拷贝各类型场景为标量复制的数十倍），
     * 用同一组迭代参数会让慢通道整体耗时过长；故迭代参数开放给调用方，
     * 由调用方按用例耗时量级选择（默认值见 {@link #WARMUP_ITERATIONS} /
     * {@link #MEASURE_ITERATIONS} / {@link #MEASURE_ROUNDS}）。</p>
     *
     * @param cases             基准用例列表
     * @param title             报告标题
     * @param warmupIterations  预热次数（不计时，触发 JIT 编译）
     * @param measureIterations 单轮计时迭代次数
     * @param measureRounds     采样轮数
     * @return 基准报告（可用于导出 markdown 文件）
     */
    public static CBenchmarkReport run(
            List<CBenchmarkCase> cases, String title,
            int warmupIterations, int measureIterations, int measureRounds) {

        // 第一轮：对所有用例预热，触发全部实现方式初始化/加载（结果不计入）
        for (CBenchmarkCase bc : cases) {

            bc.prepare();

            for (int i = 0; i < warmupIterations; i++) {
                bc.run();
            }
        }

        // 第二轮：对每个用例做多轮采样（每轮先预热再计时），取平均
        List<CBenchmarkResult> results = new ArrayList<>();

        for (CBenchmarkCase bc : cases) {

            long totalNanos = 0;
            // 逐轮记录"单次均摊耗时"：离散度由此得出（规范要求 ≥3 次有效采样 + 报离散度）
            double[] roundAvgNanos = new double[measureRounds];

            for (int round = 0; round < measureRounds; round++) {

                // 数据构造/对象初始化都在计时区间之外（prepare 不计时）
                bc.prepare();

                // 每轮开始前充分预热，保证测量在稳态下进行（预热也不计时）
                for (int i = 0; i < warmupIterations; i++) {
                    bc.run();
                }

                // 各轮等状态起步：先归集一次垃圾再计时，避免"上一轮的 GC 落在本轮计时区间内"
                // 把堆压力变成轮间离散度（对深拷贝这类高分配场景尤其明显）
                System.gc();

                long blackhole = 0;
                long start = System.nanoTime();
                for (int i = 0; i < measureIterations; i++) {
                    // 测完真的消费结果（累加进 blackhole），否则 JIT 可能整体消除被测代码
                    blackhole += System.identityHashCode(bc.run());
                }
                long elapsed = System.nanoTime() - start;

                totalNanos += elapsed;
                roundAvgNanos[round] = elapsed * 1.0 / measureIterations;

                // 防止 JIT 消除，blackhole 仅参与一次无副作用累加
                if (blackhole == Long.MIN_VALUE) {
                    log.debug("unreachable");
                }
            }

            // 累计全部迭代的耗时与迭代次数，并保留逐轮单次均摊耗时（离散度取值来源）
            results.add(CBenchmarkResult.builder()
                .name(bc.name())
                .iterations((long) measureIterations * measureRounds)
                .elapsedNanos(totalNanos)
                .roundAvgNanos(roundAvgNanos)
                .build());
        }

        // 排序按均值升序；组间差异是否显著另按离散度判定（见 CBenchmarkReport#toMarkdown 的"显著差异"标注）
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
        double baselineDispersion = report.getResults().get(0).dispersionNanos();

        log.info(String.format("%-24s %16s %16s %14s %16s", "实现方式", "Avg(ns/op)", "离散度(极差)", "ops/s", "相对基线"));
        log.info("----------------------------------------------------------------------------------------------------");
        for (CBenchmarkResult result : report.getResults()) {
            double delta = result.avgNanos() - baseline;
            // 差异小于两者离散度之和即视为无显著差异（不为噪声宣称胜出）
            boolean significant = Math.abs(delta) > (result.dispersionNanos() + baselineDispersion);
            log.info(String.format("%-24s %16.1f %16.1f %14.0f %12.2fx %s",
                    result.getName(),
                    result.avgNanos(),
                    result.dispersionNanos(),
                    result.opsPerSecond(),
                    result.avgNanos() / baseline,
                    significant || result == report.getResults().get(0) ? "" : "(无显著差异)"
            ));
        }
    }

}
