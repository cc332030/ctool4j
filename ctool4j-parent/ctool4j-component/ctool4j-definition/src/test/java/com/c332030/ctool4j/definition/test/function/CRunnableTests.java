package com.c332030.ctool4j.definition.test.function;

import com.c332030.ctool4j.definition.function.CRunnable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>
 * Description: CRunnableTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「run / 工具方法」两个维度组织。</li>
 *   <li>run 覆盖正常、受检异常；工具覆盖 EMPTY、静态 run（null/正常）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 @SneakyThrows 包装与工具方法的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：run 正常/受检异常；EMPTY；静态 run null/正常。</li>
 *   <li>未覆盖：无。</li>
 * </ul>
 * <h2>run</h2>
 * <ul>
 *   <li>1.1 正常：执行生效（runNormal）</li>
 *   <li>1.2 受检异常：抛 IOException（runSneakyThrowsCheckedException）</li>
 * </ul>
 * <h2>工具方法</h2>
 * <ul>
 *   <li>2.1 EMPTY：不抛异常（empty）</li>
 *   <li>2.2 静态 run null：不抛异常（staticRunNullRunnable）</li>
 *   <li>2.3 静态 run 正常：执行生效（staticRunNormal）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CRunnableTests {

    /**
     * 对应测试用例 1.1：正常：执行生效
     */
    @Test
    public void runNormal() {

        AtomicBoolean flag = new AtomicBoolean(false);

        CRunnable runnable = () -> flag.set(true);

        runnable.run();

        Assertions.assertTrue(flag.get());

    }

    /**
     * 对应测试用例 1.2：受检异常：抛 IOException
     */
    @Test
    public void runSneakyThrowsCheckedException() {

        CRunnable runnable = () -> {
            throw new IOException("io error");
        };

        Assertions.assertThrowsExactly(
            IOException.class,
            runnable::run
        );

    }

    /**
     * 对应测试用例 2.1：不抛异常
     */
    @Test
    public void empty() {

        CRunnable empty = CRunnable.EMPTY;

        Assertions.assertDoesNotThrow((org.junit.jupiter.api.function.Executable)empty::run);

    }

    /**
     * 对应测试用例 2.2：静态 run null：不抛异常
     */
    @Test
    public void staticRunNullRunnable() {

        Assertions.assertDoesNotThrow(() -> CRunnable.run(null));

    }

    /**
     * 对应测试用例 2.3：静态 run 正常：执行生效
     */
    @Test
    public void staticRunNormal() {

        AtomicBoolean flag = new AtomicBoolean(false);

        CRunnable.run(() -> flag.set(true));

        Assertions.assertTrue(flag.get());

    }

}
