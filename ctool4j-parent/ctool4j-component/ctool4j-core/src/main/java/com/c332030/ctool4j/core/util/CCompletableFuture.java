package com.c332030.ctool4j.core.util;

import lombok.CustomLog;
import lombok.experimental.UtilityClass;

import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * Description: CCompletableFuture
 * </p>
 *
 * @since 2024/12/18
 */
@CustomLog
@UtilityClass
public class CCompletableFuture {

    /**
     * 异步执行任务，失败时记录日志
     *
     * @param runnable 任务
     * @return 异步任务
     */
    public CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable)
                .exceptionally(e -> {
                    log.error("异步任务失败", e);
                    return null;
                });
    }

}
