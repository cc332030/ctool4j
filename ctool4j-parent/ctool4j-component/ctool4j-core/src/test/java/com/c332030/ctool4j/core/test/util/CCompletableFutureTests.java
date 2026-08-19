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

        // 修复：异常保持异常完成状态，get() 可感知失败，不再吞异常
        // 先阻塞等待任务完成（get() 抛 ExecutionException 即代表已执行且未吞异常），
        // 再断言完成状态，避免异步任务未完成时的竞态导致 isDone() 为 false
        Assertions.assertThrows(ExecutionException.class, () -> future.get(100, TimeUnit.MILLISECONDS));
        Assertions.assertTrue(future.isDone());
        Assertions.assertTrue(future.isCompletedExceptionally());

    }

}
