package com.c332030.ctool4j.definition.function;

import lombok.SneakyThrows;

/**
 * <p>
 * Description: CRunnable
 * </p>
 * <p>
 * 注意：run 方法内部使用 @SneakyThrows 包装受检异常，调用方无法从签名感知，需自行处理实际异常（设计取舍）
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CRunnable} 为任务接口，扩展 {@code Runnable}，支持受检异常：</p>
 * <ul>
 *   <li>{@code run}：默认方法，@SneakyThrows 包装后调用 {@code runThrowable}</li>
 *   <li>{@code runThrowable}：抽象方法，可抛 Throwable</li>
 *   <li>工具：{@code EMPTY}、{@code run(runnable)}</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>run(runnable=null)</td>
 *     <td>不做处理</td>
 *   </tr>
 *   <tr>
 *     <td>EMPTY</td>
 *     <td>空实现</td>
 *   </tr>
 * </table>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>用 @SneakyThrows 简化受检异常处理。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>受检异常包装</b></p>
 * <ul>
 *   <li>{@code run} 内部 @SneakyThrows 包装（设计取舍）。</li>
 * </ul>
 * <p><b>工具方法</b></p>
 * <ul>
 *   <li>{@code run(runnable)}：runnable 为 null 时不做处理。</li>
 * </ul>
 *
 * @since 2025/1/15
 * @version 1.0
 */
@FunctionalInterface
public interface CRunnable extends Runnable {

    /**
     * 执行任务（受检异常由内部包装处理）
     */
    @Override
    @SneakyThrows
    default void run() {
        runThrowable();
    }

    /**
     * 执行任务，可抛出受检异常
     * @throws Throwable 执行过程中可能抛出的异常
     */
    void runThrowable() throws Throwable;

    /**
     * 空实现常量
     */
    CRunnable EMPTY = () -> {};

    /**
     * 执行任务（runnable 为空时不做处理）
     * @param runnable 任务
     */
    static void run(Runnable runnable) {
        if(null == runnable) {
            return;
        }
        runnable.run();
    }

}
