package com.c332030.ctool4j.core.benchmark;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CBenchmarkResultTests
 * </p>
 *
 * <p>
 * 是 {@link CBenchmarkResult} 的测试用例
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>白盒分析：{@code avgNanos()} 为 {@code elapsedNanos * 1.0 / iterations}；{@code opsPerSecond()} 为</li>
 *   <li>{@code iterations * 1.0 / (elapsedNanos / 1_000_000_000.0)}。两者均为 double 运算，需覆盖</li>
 *   <li>正常值、小数结果、零边界与除零分支。</li>
 *   <li>黑盒分析：入参为迭代次数与总耗时，出参为平均耗时（纳秒/次）与每秒执行次数；</li>
 *   <li>取值代表性覆盖典型值、1 次迭代、0 耗时、0 次迭代（除零）等。</li>
 *   <li>错误推测法：double 除零在 Java 中返回 {@code Infinity}（不抛异常），需显式断言该行为；</li>
 *   <li>小数结果（如 elapsed=1、iterations=3）验证浮点精度不被截断。</li>
 * </ul>
 * <h2>覆盖场景</h2>
 * <ul>
 *   <li>覆盖：{@code avgNanos} 正例、iterations=1、elapsed=0、iterations=0（除零 → Infinity）、小数结果；</li>
 *   <li>{@code opsPerSecond} 正例、elapsed=0（除零 → Infinity）、半耗时；</li>
 *   <li>getter 对构造字段的原样返回。</li>
 *   <li>未覆盖：超长耗时（纳秒溢出）等极端值（{@code CBenchmarkRunner} 实际传入的迭代次数固定，</li>
 *   <li>不构造此类输入）。</li>
 * </ul>
 * <h2>avgNanos()</h2>
 * <ul>
 *   <li>1.1 正例：elapsedNanos=5000、iterations=1000 → 5.0</li>
 *   <li>1.2 边界：iterations=1 → 返回 elapsedNanos 本身</li>
 *   <li>1.3 边界：elapsedNanos=0 → 0.0</li>
 *   <li>1.4 边界/除零：iterations=0 → 返回 Infinity（double 除零语义）</li>
 *   <li>1.5 边界：elapsedNanos=1、iterations=3 → 约 0.333...（小数精度）</li>
 * </ul>
 * <h2>opsPerSecond()</h2>
 * <ul>
 *   <li>2.1 正例：iterations=1000、elapsedNanos=1_000_000_000 → 1000.0</li>
 *   <li>2.2 边界/除零：elapsedNanos=0 → 返回 Infinity（double 除零语义）</li>
 *   <li>2.3 边界：iterations=1000、elapsedNanos=2_000_000_000 → 500.0</li>
 * </ul>
 * <h2>字段取值（getter）</h2>
 * <ul>
 *   <li>3.1 正例：构造后 {@code getName} / {@code getIterations} / {@code getElapsedNanos} 原样返回构造入参</li>
 * </ul>
 * <h2>逐轮采样下的 avgNanos / opsPerSecond（有离散度取值来源时）</h2>
 * <ul>
 *   <li>4.1 正例：{@code roundAvgNanos=[4,6,5]} → {@code avgNanos}=5.0（不再是 elapsedNanos/iterations 那一路）</li>
 *   <li>4.2 边界：{@code roundAvgNanos=null} → 退化为 {@code elapsedNanos / iterations}</li>
 *   <li>4.3 边界：{@code roundAvgNanos} 为空数组 → 退化为 {@code elapsedNanos / iterations}</li>
 * </ul>
 * <h2>dispersionNanos / dispersionRatio（离散度）</h2>
 * <ul>
 *   <li>5.1 正例：{@code [4,6,5]} → 极差 2.0（最慢 − 最快）</li>
 *   <li>5.2 边界：单轮（不足 2 个采样）→ 0.0（无从计算离散度）</li>
 *   <li>5.3 边界：{@code roundAvgNanos=null} → 0.0</li>
 *   <li>5.4 边界：均值为 0 → 相对离散度为 0.0（不产生除零/NaN）</li>
 *   <li>5.5 正例：{@code [4,6]} → 相对离散度 0.4（极差 2 / 均值 5）</li>
 * </ul>
 *
 * @since 2026/8/21
 * @version 1.0
 */
public class CBenchmarkResultTests {

    /**
     * 对应测试用例 1.1：正例：elapsedNanos=5000、iterations=1000 → 5.0
     */
    @Test
    public void avgNanosNormal() {

        CBenchmarkResult result = CBenchmarkResult.builder()
            .name("case")
            .iterations(1000)
            .elapsedNanos(5000)
            .build();

        Assertions.assertEquals(5.0, result.avgNanos());

    }

    /**
     * 对应测试用例 1.2：边界：iterations=1 → 返回 elapsedNanos 本身
     */
    @Test
    public void avgNanosSingleIteration() {

        CBenchmarkResult result = CBenchmarkResult.builder()
            .name("case")
            .iterations(1)
            .elapsedNanos(5000)
            .build();

        Assertions.assertEquals(5000.0, result.avgNanos());

    }

    /**
     * 对应测试用例 1.3：边界：elapsedNanos=0 → 0.0
     */
    @Test
    public void avgNanosZeroElapsed() {

        CBenchmarkResult result = CBenchmarkResult.builder()
            .name("case")
            .iterations(1000)
            .elapsedNanos(0)
            .build();

        Assertions.assertEquals(0.0, result.avgNanos());

    }

    /**
     * 对应测试用例 1.4：边界/除零：iterations=0 → 返回 Infinity（double 除零语义）
     */
    @Test
    public void avgNanosZeroIterations() {

        CBenchmarkResult result = CBenchmarkResult.builder()
            .name("case")
            .iterations(0)
            .elapsedNanos(5000)
            .build();

        // double 除零返回 Infinity，不抛异常
        Assertions.assertEquals(Double.POSITIVE_INFINITY, result.avgNanos());

    }

    /**
     * 对应测试用例 1.5：边界：elapsedNanos=1、iterations=3 → 约 0.333...（小数精度）
     */
    @Test
    public void avgNanosFraction() {

        CBenchmarkResult result = CBenchmarkResult.builder()
            .name("case")
            .iterations(3)
            .elapsedNanos(1)
            .build();

        Assertions.assertEquals(1.0 / 3.0, result.avgNanos(), 1e-12);

    }

    /**
     * 对应测试用例 2.1：正例：iterations=1000、elapsedNanos=1_000_000_000 → 1000.0
     */
    @Test
    public void opsPerSecondNormal() {

        CBenchmarkResult result = CBenchmarkResult.builder()
            .name("case")
            .iterations(1000)
            .elapsedNanos(1_000_000_000)
            .build();

        Assertions.assertEquals(1000.0, result.opsPerSecond());

    }

    /**
     * 对应测试用例 2.2：边界/除零：elapsedNanos=0 → 返回 Infinity（double 除零语义）
     */
    @Test
    public void opsPerSecondZeroElapsed() {

        CBenchmarkResult result = CBenchmarkResult.builder()
            .name("case")
            .iterations(1000)
            .elapsedNanos(0)
            .build();

        // double 除零返回 Infinity，不抛异常
        Assertions.assertEquals(Double.POSITIVE_INFINITY, result.opsPerSecond());

    }

    /**
     * 对应测试用例 2.3：边界：iterations=1000、elapsedNanos=2_000_000_000 → 500.0
     */
    @Test
    public void opsPerSecondHalfTime() {

        CBenchmarkResult result = CBenchmarkResult.builder()
            .name("case")
            .iterations(1000)
            .elapsedNanos(2_000_000_000)
            .build();

        Assertions.assertEquals(500.0, result.opsPerSecond());

    }

    /**
     * 对应测试用例 3.1：正例：构造后 {@code getName} / {@code getIterations} / {@code getElapsedNanos} 原样返回构造入参
     */
    @Test
    public void fieldsReturnedAsIs() {

        CBenchmarkResult result = CBenchmarkResult.builder()
            .name("copy")
            .iterations(100)
            .elapsedNanos(200)
            .build();

        Assertions.assertEquals("copy", result.getName());
        Assertions.assertEquals(100, result.getIterations());
        Assertions.assertEquals(200, result.getElapsedNanos());

    }

    /**
     * 对应测试用例 4.1：正例：有逐轮采样时 avgNanos 取各轮单次均摊耗时的均值（而非 elapsedNanos/iterations）
     */
    @Test
    public void avgNanosUsesRoundAverages() {

        // elapsedNanos/iterations = 1000/10 = 100，但逐轮均值是 5 —— 断言取的是后者
        CBenchmarkResult result = CBenchmarkResult.builder()
            .name("case")
            .iterations(10)
            .elapsedNanos(1000)
            .roundAvgNanos(new double[] {4.0, 6.0, 5.0})
            .build();

        Assertions.assertEquals(5.0, result.avgNanos());

    }

    /**
     * 对应测试用例 4.2：边界：无逐轮采样（null）→ 退化为 elapsedNanos / iterations
     */
    @Test
    public void avgNanosFallsBackWhenRoundsAbsent() {

        CBenchmarkResult result = CBenchmarkResult.builder()
            .name("case")
            .iterations(1000)
            .elapsedNanos(5000)
            .build();

        Assertions.assertEquals(5.0, result.avgNanos());

    }

    /**
     * 对应测试用例 4.3：边界：逐轮采样为空数组 → 退化为 elapsedNanos / iterations
     */
    @Test
    public void avgNanosFallsBackWhenRoundsEmpty() {

        CBenchmarkResult result = CBenchmarkResult.builder()
            .name("case")
            .iterations(1000)
            .elapsedNanos(5000)
            .roundAvgNanos(new double[0])
            .build();

        Assertions.assertEquals(5.0, result.avgNanos());

    }

    /**
     * 对应测试用例 5.1：正例：离散度取极差（最慢 − 最快）
     */
    @Test
    public void dispersionNanosIsRange() {

        CBenchmarkResult result = CBenchmarkResult.builder()
            .name("case")
            .iterations(1)
            .elapsedNanos(1)
            .roundAvgNanos(new double[] {4.0, 6.0, 5.0})
            .build();

        Assertions.assertEquals(2.0, result.dispersionNanos());

    }

    /**
     * 对应测试用例 5.2：边界：单轮采样 → 离散度 0（不足 2 个采样无从计算）
     */
    @Test
    public void dispersionNanosZeroForSingleRound() {

        CBenchmarkResult result = CBenchmarkResult.builder()
            .name("case")
            .iterations(1)
            .elapsedNanos(1)
            .roundAvgNanos(new double[] {4.0})
            .build();

        Assertions.assertEquals(0.0, result.dispersionNanos());

    }

    /**
     * 对应测试用例 5.3：边界：无逐轮采样（null）→ 离散度 0
     */
    @Test
    public void dispersionNanosZeroWhenRoundsAbsent() {

        CBenchmarkResult result = CBenchmarkResult.builder()
            .name("case")
            .iterations(1)
            .elapsedNanos(1)
            .build();

        Assertions.assertEquals(0.0, result.dispersionNanos());

    }

    /**
     * 对应测试用例 5.4：边界：均值为 0 → 相对离散度 0（不产生除零/NaN）
     */
    @Test
    public void dispersionRatioZeroWhenAverageZero() {

        CBenchmarkResult result = CBenchmarkResult.builder()
            .name("case")
            .iterations(1)
            .elapsedNanos(0)
            .roundAvgNanos(new double[] {0.0, 0.0})
            .build();

        Assertions.assertEquals(0.0, result.dispersionRatio());

    }

    /**
     * 对应测试用例 5.5：正例：相对离散度 = 极差 / 均值
     */
    @Test
    public void dispersionRatioIsRangeOverAverage() {

        CBenchmarkResult result = CBenchmarkResult.builder()
            .name("case")
            .iterations(1)
            .elapsedNanos(1)
            .roundAvgNanos(new double[] {4.0, 6.0})
            .build();

        // 均值 5、极差 2 → 0.4
        Assertions.assertEquals(0.4, result.dispersionRatio());

    }

}
