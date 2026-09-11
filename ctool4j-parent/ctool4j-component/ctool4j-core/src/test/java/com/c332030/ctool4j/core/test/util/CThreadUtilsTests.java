package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CThreadUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Description: CThreadUtilsTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「命名重载 / 默认命名 / 可执行性」三个维度组织。</li>
 *   <li>命名重载验证线程名与 daemon 标志；默认命名验证前缀 {@code DaemonThread-} 与 daemon。</li>
 *   <li>可执行性：启动线程后经 CountDownLatch 验证任务实际执行，且线程为 daemon。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 daemon 与默认命名的约定。</li>
 *   <li>依据测试方法（等价类/边界值）：命名/默认命名、任务执行验证。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：自定义命名、默认命名（前缀校验）、任务可执行且线程为 daemon。</li>
 *   <li>未覆盖：不阻止 JVM 退出的端到端验证（daemon 语义由 {@code isDaemon()} 间接保证）。</li>
 * </ul>
 * <h2>创建守护线程</h2>
 * <ul>
 *   <li>1.1 命名重载：线程名正确且为 daemon（newDaemonThreadWithName）</li>
 *   <li>1.2 默认命名：前缀为 {@code DaemonThread-} 且为 daemon（newDaemonThreadDefaultName）</li>
 *   <li>1.3 可执行性：启动后任务执行成功（CountDownLatch）且为 daemon（newDaemonThreadRunnableExecutes）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CThreadUtilsTests {

    /**

     * 对应测试用例 1.1：命名重载：线程名正确且为 daemon

     */
    @Test
    public void newDaemonThreadWithName() {

        Thread thread = CThreadUtils.newDaemonThread(() -> {
        }, "my-thread");

        Assertions.assertEquals("my-thread", thread.getName());
        Assertions.assertTrue(thread.isDaemon());

    }

    /**

     * 对应测试用例 1.2：默认命名：前缀为 {@code DaemonThread-} 且为 daemon

     */
    @Test
    public void newDaemonThreadDefaultName() {

        Thread thread = CThreadUtils.newDaemonThread(() -> {
        });

        Assertions.assertTrue(thread.getName().startsWith("DaemonThread-"));
        Assertions.assertTrue(thread.isDaemon());

    }
    /**
     * 对应测试用例 1.3：可执行性：启动后任务执行成功（CountDownLatch）且为 daemon
     */

    @Test
    public void newDaemonThreadRunnableExecutes() throws Exception {

        CountDownLatch latch = new CountDownLatch(1);
        Thread thread = CThreadUtils.newDaemonThread(latch::countDown, "work");

        thread.start();
        Assertions.assertTrue(latch.await(5, TimeUnit.SECONDS));
        Assertions.assertTrue(thread.isDaemon());

    }

}
