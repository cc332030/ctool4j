package com.c332030.ctool4j.core.util;

import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * <p>
 * Description: CThreadUtils
 * </p>
 *
 * @since 2026/8/14
 * @see "doc/design/core/CThreadUtils.adoc"
 * @see "doc/design/core/CThreadUtilsTests.adoc"
 */
@UtilityClass
public class CThreadUtils {

    /**
     * 创建守护线程（默认命名）
     *
     * @param runnable 任务
     * @return 守护线程
     */
    public Thread newDaemonThread(Runnable runnable) {
        return newDaemonThread(runnable, "DaemonThread-" + System.currentTimeMillis());
    }

    /**
     * 创建守护线程
     *
     * @param runnable 任务
     * @param name     线程名
     * @return 守护线程
     */
    public Thread newDaemonThread(Runnable runnable, String name) {
        val thread = new Thread(runnable, name);
        thread.setDaemon(true);
        return thread;
    }

}
