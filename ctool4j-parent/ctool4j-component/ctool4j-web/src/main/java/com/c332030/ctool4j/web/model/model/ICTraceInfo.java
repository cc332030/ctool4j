package com.c332030.ctool4j.web.model.model;

/**
 * <p>
 * Description: ICTraceInfo
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICTraceInfo} 为链路追踪信息接口，声明 {@code traceId} 的读写方法：</p>
 * <ul>
 *   <li>{@code getTraceId()}：获取链路追踪 ID</li>
 *   <li>{@code setTraceId(String)}：设置链路追踪 ID</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>无</td>
 *     <td>纯接口契约</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要携带链路追踪 ID 的模型类实现本接口。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>仅声明读写方法，无默认实现。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>统一追踪模型</b></p>
 * <ul>
 *   <li>定义携带链路追踪 ID 的模型契约，供 {@code CTraceInfo} 等实现。</li>
 * </ul>
 *
 * @since 2025/9/26
 * @version 1.0
 */
public interface ICTraceInfo {

    /**
     * 获取链路追踪ID
     * @return 链路追踪ID
     */
    String getTraceId();

    /**
     * 设置链路追踪ID
     * @param traceId 链路追踪ID
     */
    void setTraceId(String traceId);

}
