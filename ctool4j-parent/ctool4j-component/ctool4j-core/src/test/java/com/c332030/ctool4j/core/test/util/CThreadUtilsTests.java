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
 * @since 2026/8/14
 */
public class CThreadUtilsTests {

    @Test
    public void newDaemonThreadWithName() {

        Thread thread = CThreadUtils.newDaemonThread(() -> {
        }, "my-thread");

        Assertions.assertEquals("my-thread", thread.getName());
        Assertions.assertTrue(thread.isDaemon());

    }

    @Test
    public void newDaemonThreadDefaultName() {

        Thread thread = CThreadUtils.newDaemonThread(() -> {
        });

        Assertions.assertTrue(thread.getName().startsWith("DaemonThread-"));
        Assertions.assertTrue(thread.isDaemon());

    }

    @Test
    public void newDaemonThreadRunnableExecutes() throws Exception {

        CountDownLatch latch = new CountDownLatch(1);
        Thread thread = CThreadUtils.newDaemonThread(latch::countDown, "work");

        thread.start();
        Assertions.assertTrue(latch.await(5, TimeUnit.SECONDS));
        Assertions.assertTrue(thread.isDaemon());

    }

}
