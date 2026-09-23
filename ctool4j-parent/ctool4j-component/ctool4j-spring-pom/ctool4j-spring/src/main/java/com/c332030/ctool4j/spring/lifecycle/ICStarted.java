package com.c332030.ctool4j.spring.lifecycle;

/**
 * <p>
 * Description: ICStarted
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICStarted}：应用启动完成回调接口。</p>
 * <ul>
 *   <li>{@code onStarted()}：由 {@code CStartedApplicationRunner} 在 {@code SpringApplication.run}
 *   执行完成后回调。</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>与 {@link ICSpringInit} 的分工：{@code onInit} 在单例实例化完成后触发，{@code onStarted}
 *   在整个容器就绪、且 {@code SpringApplication.run} 的启动流程执行到最后一阶段时触发。</li>
 *   <li>回调由既有的 {@code CStartedApplicationRunner} 承载，不额外新增运行器：容器内既有的运行器
 *   若都实现本接口，启动完成点就只有一个真源，不会出现第二套"启动完成"回调。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>实现类无启动后处理</td>
 *     <td>默认实现为空操作，实现类只覆写需要的方法</td>
 *   </tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要在 {@code SpringApplication.run} 执行完成后处理一次的场景（预计算、聚合、释放启动期资源）。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要 Spring 容器；不处于 {@code SpringApplication.run} 启动流程内的容器不具备该回调点。</li>
 *   <li>本接口只定义回调点，不负责阻止实现类被当作运行器注册：仅实现本接口不产生回调，
 *   须同时实现 {@code ApplicationRunner}（或实现 {@code CStartedApplicationRunner}）。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>回调在启动流程内同步执行，耗时操作会延长启动时间。</li>
 * </ul>
 *
 * @since 2026/9/23
 * @version 1.0
 */
public interface ICStarted {

    /**
     * 应用启动完成回调（{@code SpringApplication.run} 执行完成后触发）
     */
    default void onStarted() {

    }

}
