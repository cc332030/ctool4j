package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CCompletableFuture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Description: CCompletableFutureTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CCompletableFutureTests {

    @Test
    public void runAsyncNormal() throws Exception {

        AtomicInteger run = new AtomicInteger();
        CompletableFuture<Void> future = CCompletableFuture.runAsync(run::incrementAndGet);

        Assertions.assertNull(future.get(5, TimeUnit.SECONDS));
        Assertions.assertEquals(1, run.get());

    }

    @Test
    public void runAsyncExceptionPropagated() throws Exception {

        CompletableFuture<Void> future = CCompletableFuture.runAsync(() -> {
            throw new IllegalStateException("boom");
        });

        // Q22 修复：异常保持异常完成状态，get() 可感知失败，不再吞异常
        Assertions.assertTrue(future.isDone());
        Assertions.assertTrue(future.isCompletedExceptionally());
        Assertions.assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS));

    }

}
