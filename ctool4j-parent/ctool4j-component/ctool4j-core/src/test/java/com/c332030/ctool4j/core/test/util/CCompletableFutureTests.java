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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「正常执行 / 异常传播」两个维度组织。</li>
 *   <li>正常执行：任务自增 AtomicInteger，get() 返回 null 且自增生效，验证异步执行成功。</li>
 *   <li>异常传播：任务抛异常，断言 get() 抛 ExecutionException，且 Future 处于异常完成状态</li>
 *   <li>（isDone 与 isCompletedExceptionally 为 true），验证不吞异常的核心语义。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对不吞异常、保留异常完成状态的约定。</li>
 *   <li>依据测试方法（等价类/边界值/异步时序）：正常任务、异常任务、完成状态断言。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：正常异步执行（结果 null + 副作用生效）、异常传播（get 抛 ExecutionException、isDone、</li>
 *   <li>isCompletedExceptionally）。</li>
 *   <li>未覆盖：任务耗时超时、拒绝策略等（非本类关注点）。</li>
 * </ul>
 * <h2>正常执行</h2>
 * <ul>
 *   <li>1.1 异步执行：任务自增生效，get() 返回 null（runAsyncNormal）</li>
 * </ul>
 * <h2>异常传播</h2>
 * <ul>
 *   <li>2.1 异常保持：任务抛异常，get() 抛 ExecutionException，isDone 与 isCompletedExceptionally 为 true（runAsyncExceptionPropagated）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CCompletableFutureTests {
    /**
     * 对应测试用例 1.1：异步执行：任务自增生效，get() 返回 null
     */

    @Test
    public void runAsyncNormal() throws Exception {

        AtomicInteger run = new AtomicInteger();
        CompletableFuture<Void> future = CCompletableFuture.runAsync(run::incrementAndGet);

        Assertions.assertNull(future.get(5, TimeUnit.SECONDS));
        Assertions.assertEquals(1, run.get());

    }
    /**
     * 对应测试用例 2.1：异常保持：任务抛异常，get() 抛 ExecutionException，isDone 与 isCompletedExceptionally 为 true
     */

    @Test
    public void runAsyncExceptionPropagated() throws Exception {

        CompletableFuture<Void> future = CCompletableFuture.runAsync(() -> {
            throw new IllegalStateException("boom");
        });

        // 修复：异常保持异常完成状态，get() 可感知失败，不再吞异常
        // 先阻塞等待任务完成（get() 抛 ExecutionException 即代表已执行且未吞异常），
        // 再断言完成状态，避免异步任务未完成时的竞态导致 isDone() 为 false
        Assertions.assertThrowsExactly(ExecutionException.class, () -> future.get(100, TimeUnit.MILLISECONDS));
        Assertions.assertTrue(future.isDone());
        Assertions.assertTrue(future.isCompletedExceptionally());

    }

}
