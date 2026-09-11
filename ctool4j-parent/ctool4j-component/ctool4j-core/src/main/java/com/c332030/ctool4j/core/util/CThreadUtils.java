package com.c332030.ctool4j.core.util;

import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * <p>
 * Description: CThreadUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CThreadUtils} 为线程工具类，提供：</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>未命名重载</td>
 *     <td>使用默认名 {@code DaemonThread-{currentTimeMillis}}</td>
 *   </tr>
 *   <tr>
 *     <td>命名重载</td>
 *     <td>使用调用方传入的线程名</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要后台执行、不应阻止 JVM 退出的辅助任务线程（如定时清理、异步非关键任务）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>仅创建线程，不负责启动（{@code start()}）；不提供线程池封装。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>强制 daemon，避免未管理线程阻塞 JVM 退出；调用方需自行管理生命周期。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>守护线程约定</b></p>
 * <ul>
 *   <li>创建的线程均设为 daemon（{@code setDaemon(true)}），不阻止 JVM 退出。</li>
 *   <li>默认命名用 {@code System.currentTimeMillis()} 保证基础唯一性。</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
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
