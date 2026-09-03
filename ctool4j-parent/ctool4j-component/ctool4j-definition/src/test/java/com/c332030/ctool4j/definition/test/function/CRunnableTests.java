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
 * @since 2026/8/14
 */
public class CRunnableTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void runNormal() {

        AtomicBoolean flag = new AtomicBoolean(false);

        CRunnable runnable = () -> flag.set(true);

        runnable.run();

        Assertions.assertTrue(flag.get());

    }

    /**
     * 对应测试用例 1.2
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
     * 对应测试用例 2.1
     */
    @Test
    public void empty() {

        CRunnable empty = CRunnable.EMPTY;

        Assertions.assertDoesNotThrow((org.junit.jupiter.api.function.Executable)empty::run);

    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void staticRunNullRunnable() {

        Assertions.assertDoesNotThrow(() -> CRunnable.run(null));

    }

    /**
     * 对应测试用例 2.3
     */
    @Test
    public void staticRunNormal() {

        AtomicBoolean flag = new AtomicBoolean(false);

        CRunnable.run(() -> flag.set(true));

        Assertions.assertTrue(flag.get());

    }

}
