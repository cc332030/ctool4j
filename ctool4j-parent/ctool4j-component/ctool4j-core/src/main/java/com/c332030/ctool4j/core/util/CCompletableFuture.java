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
     * <p>使用 whenComplete 仅记录日志，异常仍保持异常完成状态，
     * 调用方 get()/join() 时可感知失败，避免吞异常误判成功</p>
     *
     * @param runnable 任务
     * @return 异步任务
     */
    public CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable)
                .whenComplete((value, e) -> {
                    if(null != e) {
                        log.error("异步任务失败", e);
                    }
                });
    }

}
