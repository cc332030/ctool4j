package com.c332030.ctool4j.core.test.benchmark;

import com.c332030.ctool4j.core.benchmark.CBenchmarkResult;
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
 * @since 2026/8/21
 */
public class CBenchmarkResultTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void avgNanosNormal() {

        CBenchmarkResult result = new CBenchmarkResult("case", 1000, 5000);

        Assertions.assertEquals(5.0, result.avgNanos());

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void avgNanosSingleIteration() {

        CBenchmarkResult result = new CBenchmarkResult("case", 1, 5000);

        Assertions.assertEquals(5000.0, result.avgNanos());

    }

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void avgNanosZeroElapsed() {

        CBenchmarkResult result = new CBenchmarkResult("case", 1000, 0);

        Assertions.assertEquals(0.0, result.avgNanos());

    }

    /**
     * 对应测试用例 1.4
     */
    @Test
    public void avgNanosZeroIterations() {

        CBenchmarkResult result = new CBenchmarkResult("case", 0, 5000);

        // double 除零返回 Infinity，不抛异常
        Assertions.assertEquals(Double.POSITIVE_INFINITY, result.avgNanos());

    }

    /**
     * 对应测试用例 1.5
     */
    @Test
    public void avgNanosFraction() {

        CBenchmarkResult result = new CBenchmarkResult("case", 3, 1);

        Assertions.assertEquals(1.0 / 3.0, result.avgNanos(), 1e-12);

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void opsPerSecondNormal() {

        CBenchmarkResult result = new CBenchmarkResult("case", 1000, 1_000_000_000);

        Assertions.assertEquals(1000.0, result.opsPerSecond());

    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void opsPerSecondZeroElapsed() {

        CBenchmarkResult result = new CBenchmarkResult("case", 1000, 0);

        // double 除零返回 Infinity，不抛异常
        Assertions.assertEquals(Double.POSITIVE_INFINITY, result.opsPerSecond());

    }

    /**
     * 对应测试用例 2.3
     */
    @Test
    public void opsPerSecondHalfTime() {

        CBenchmarkResult result = new CBenchmarkResult("case", 1000, 2_000_000_000);

        Assertions.assertEquals(500.0, result.opsPerSecond());

    }

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void fieldsReturnedAsIs() {

        CBenchmarkResult result = new CBenchmarkResult("copy", 100, 200);

        Assertions.assertEquals("copy", result.getName());
        Assertions.assertEquals(100, result.getIterations());
        Assertions.assertEquals(200, result.getElapsedNanos());

    }

}
