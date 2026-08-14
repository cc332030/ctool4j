package com.c332030.ctool4j.core.util;

import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * <p>
 * Description: CThreadUtils
 * </p>
 *
 * @since 2026/8/14
 */
@UtilityClass
public class CThreadUtils {

    /**
     * 创建守护线程（默认命名）
     *
     * @param runnable 任务
     * @return 守护线程
     */
    public Thread newDeamonThread(Runnable runnable) {
        return newDeamonThread(runnable, "DeamonThread-" + System.currentTimeMillis());
    }

    /**
     * 创建守护线程
     *
     * @param runnable 任务
     * @param name     线程名
     * @return 守护线程
     */
    public Thread newDeamonThread(Runnable runnable, String name) {
        val thread = new Thread(runnable, name);
        thread.setDaemon(true);
        return thread;
    }

}
