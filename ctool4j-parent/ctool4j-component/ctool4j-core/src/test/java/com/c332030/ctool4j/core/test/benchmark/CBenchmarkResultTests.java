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
 * @see "doc/design/core/CBenchmarkResultTests.adoc"
 */
public class CBenchmarkResultTests {

    /**
     * 对应测试用例 1.1
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
     * 对应测试用例 1.2
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
     * 对应测试用例 1.3
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
     * 对应测试用例 1.4
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
     * 对应测试用例 1.5
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
     * 对应测试用例 2.1
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
     * 对应测试用例 2.2
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
     * 对应测试用例 2.3
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
     * 对应测试用例 3.1
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

}
