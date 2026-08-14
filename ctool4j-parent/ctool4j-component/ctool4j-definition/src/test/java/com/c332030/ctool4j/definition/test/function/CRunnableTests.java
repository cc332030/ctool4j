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

    @Test
    public void runNormal() {

        AtomicBoolean flag = new AtomicBoolean(false);

        CRunnable runnable = () -> flag.set(true);

        runnable.run();

        Assertions.assertTrue(flag.get());

    }

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

    @Test
    public void empty() {

        CRunnable empty = CRunnable.EMPTY;

        Assertions.assertDoesNotThrow((org.junit.jupiter.api.function.Executable)empty::run);

    }

    @Test
    public void staticRunNullRunnable() {

        Assertions.assertDoesNotThrow(() -> CRunnable.run(null));

    }

    @Test
    public void staticRunNormal() {

        AtomicBoolean flag = new AtomicBoolean(false);

        CRunnable.run(() -> flag.set(true));

        Assertions.assertTrue(flag.get());

    }

    @Test
    public void convert() {

        AtomicBoolean flag = new AtomicBoolean(false);

        CRunnable cRunnable = () -> flag.set(true);

        Runnable runnable = CRunnable.convert(cRunnable);

        runnable.run();

        Assertions.assertTrue(flag.get());

    }

    @Test
    public void convertNullCRunnable() {

        Runnable runnable = CRunnable.convert(null);

        Assertions.assertDoesNotThrow(runnable::run);

    }

}
