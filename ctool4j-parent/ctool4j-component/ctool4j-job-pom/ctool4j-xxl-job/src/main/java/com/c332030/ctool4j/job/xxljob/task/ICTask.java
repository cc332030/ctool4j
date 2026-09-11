package com.c332030.ctool4j.job.xxljob.task;

import com.c332030.ctool4j.spring.service.ICProxyService;

/**
 * <p>
 * Description: ICTask
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICTask&lt;T extends ICTask&lt;T&gt;&gt;}（extends {@code ICProxyService&lt;T&gt;}）定义任务执行接口：</p>
 * <ul>
 *   <li>{@code execute()}：无参执行，内部调用 {@code execute(null)}。</li>
 *   <li>{@code execute(String param)}：默认抛出 {@code UnsupportedOperationException}，由子类实现。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>子类未实现 execute(String)</td>
 *     <td>抛 UnsupportedOperationException</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>定义统一的 xxl-job 任务执行契约。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>依赖 ICProxyService 的代理机制。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>默认行为</b></p>
 * <ul>
 *   <li>{@code execute()} 经 {@code currentProxy()} 调用 {@code execute(null)}。</li>
 *   <li>{@code execute(String)} 默认抛异常，提示子类实现。</li>
 * </ul>
 *
 * @since 2025/12/26
 * @version 1.0
 */
public interface ICTask<T extends ICTask<T>> extends ICProxyService<T> {

    /**
     * 执行任务（无参数）
     */
    default void execute() {
        currentProxy().execute(null);
    }

    /**
     * 执行任务
     * @param param 任务参数
     */
    default void execute(String param) {
        throw new UnsupportedOperationException();
    }

}
