package com.c332030.ctool4j.core.benchmark;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * <p>
 * Description: 轻量性能基准运行器（main 入口）
 * </p>
 * <p>
 * 架构说明：性能测试代码置于测试源码目录（独立包 benchmark，无 @Test、命名不匹配
 * surefire 测试类规则），不随 mvn test 执行、不参与打包；仅通过明确命令
 * "mvn test-compile exec:java" 触发本类 main 执行。
 * </p>
 * <p>
 * 流程：共测试两次。第一轮对所有用例预热，触发全部实现方式初始化/加载
 * （初始化干扰不计入结果）；第二轮在全部初始化完成后再预热并正式计时，
 * run 返回值经 identityHashCode 累计，防止 JIT 将无副作用的循环体消除。
 * </p>
 *
 * @since 2026/8/16
 */
public class BenchmarkRunner {

    /**
     * 预热次数（不计时，触发 JIT 编译）
     */
    private static final int WARMUP_ITERATIONS = 50_000;

    /**
     * 计时迭代次数
     */
    private static final int MEASURE_ITERATIONS = 100_000;

    public static void main(String[] args) {

        List<BenchmarkCase> cases = CBeanUtilsBenchmark.cases();

        // 第一轮：对所有用例预热，触发全部实现方式初始化/加载（结果不计入）
        for (BenchmarkCase bc : cases) {

            bc.prepare();

            for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                bc.run();
            }
        }

        // 第二轮：全部初始化完成后，再预热并正式计时
        List<BenchmarkResult> results = new ArrayList<>();

        for (BenchmarkCase bc : cases) {

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

            results.add(new BenchmarkResult(bc.name(), MEASURE_ITERATIONS, elapsed));

            // 防止 JIT 消除，blackhole 仅参与一次无副作用累加
            if (blackhole == Long.MIN_VALUE) {
                System.out.println("unreachable");
            }
        }

        print(results);
    }

    private static void print(List<BenchmarkResult> results) {

        results.sort(Comparator.comparingDouble(BenchmarkResult::avgNanos));

        double baseline = results.get(0).avgNanos();

        System.out.println();
        System.out.println("===== CBeanUtils 属性复制性能对比（" + MEASURE_ITERATIONS + " 次迭代）=====");
        System.out.printf("%-24s %16s %16s %14s%n", "实现方式", "Avg(ns/op)", "ops/s", "相对基线");
        System.out.println("--------------------------------------------------------------------------");
        for (BenchmarkResult result : results) {
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
