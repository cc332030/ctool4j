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

    public Thread newDeamonThread(Runnable runnable) {
        return newDeamonThread(runnable, "DeamonThread-" + System.currentTimeMillis());
    }

    public Thread newDeamonThread(Runnable runnable, String name) {
        val thread = new Thread(runnable, name);
        thread.setDaemon(true);
        return thread;
    }

}
