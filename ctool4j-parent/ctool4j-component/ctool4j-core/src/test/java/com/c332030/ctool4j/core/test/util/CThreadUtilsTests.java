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
    public void newDeamonThreadWithName() {

        Thread thread = CThreadUtils.newDeamonThread(() -> {
        }, "my-thread");

        Assertions.assertEquals("my-thread", thread.getName());
        Assertions.assertTrue(thread.isDaemon());

    }

    @Test
    public void newDeamonThreadDefaultName() {

        Thread thread = CThreadUtils.newDeamonThread(() -> {
        });

        Assertions.assertTrue(thread.getName().startsWith("DeamonThread-"));
        Assertions.assertTrue(thread.isDaemon());

    }

    @Test
    public void newDeamonThreadRunnableExecutes() throws Exception {

        CountDownLatch latch = new CountDownLatch(1);
        Thread thread = CThreadUtils.newDeamonThread(latch::countDown, "work");

        thread.start();
        Assertions.assertTrue(latch.await(5, TimeUnit.SECONDS));
        Assertions.assertTrue(thread.isDaemon());

    }

}
