package com.c332030.ctool4j.core.util;

import lombok.CustomLog;
import lombok.experimental.UtilityClass;

import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * Description: CCompletableFuture
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CCompletableFuture} 为异步任务工具类，提供：</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>任务执行成功</td>
 *     <td>Future 正常完成，get() 返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>任务抛异常</td>
 *     <td>记录 error 日志，Future 异常完成，get() 抛 ExecutionException</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要异步执行任务，且需要失败日志记录，同时不丢失失败语义的场景。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>仅支持 {@code runAsync}（无返回值），需要异步结果时需配合外部状态（如 Atomic 变量）。</li>
 *   <li>不提供拒绝策略/线程池定制（复用 JDK 默认公共 ForkJoinPool）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>保留异常完成状态（而非吞掉）是本类核心取舍，保证调用方能感知失败。</li>
 *   <li>使用默认线程池，无自定义 executor 重载；高并发需自行管理线程池时用原生 CompletableFuture。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>异常语义</b></p>
 * <ul>
 *   <li>底层 {@code CompletableFuture.runAsync(runnable)}，再经 {@code whenComplete} 仅在异常时记录错误日志。</li>
 *   <li><b>不吞异常</b>：whenComplete 只记录日志，不改变 Future 的完成状态；任务异常时 Future 仍以</li>
 *   <li>异常完成，调用方 {@code get()/join()} 会抛 {@code ExecutionException}，可感知失败，避免误判成功。</li>
 * </ul>
 *
 * @since 2024/12/18
 * @version 1.0
 */
@CustomLog
@UtilityClass
public class CCompletableFuture {

    /**
     * 异步执行任务，失败时记录日志
     *
     * <p>使用 whenComplete 仅记录日志，异常仍保持异常完成状态，
     * 调用方 get()/join() 时可感知失败，避免吞异常误判成功</p>
     * <ul>
     *   <li>{@code runAsync(Runnable)}：异步执行任务；成功返回 {@code null} 结果，失败记录日志但 Future 保持异常完成状态。</li>
     * </ul>
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
