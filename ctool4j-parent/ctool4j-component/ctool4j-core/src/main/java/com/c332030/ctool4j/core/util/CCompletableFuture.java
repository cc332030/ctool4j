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

    public CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable)
                .exceptionally(e -> {
                    log.error("异步任务失败", e);
                    return null;
                });
    }

}
