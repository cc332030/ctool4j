package com.c332030.ctool4j.web.spi;

import com.c332030.ctool4j.web.model.model.ICTraceInfo;

/**
 * <p>
 * Description: ICTraceInfoProvider
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICTraceInfoProvider&lt;T extends ICTraceInfo&gt;} 为链路追踪信息提供者接口：</p>
 * <ul>
 *   <li>{@code getTraceInfo()}：获取链路追踪信息（返回 {@code T extends ICTraceInfo}）</li>
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
 *   <li>需要向框架提供链路追踪信息的实现方实现本接口。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>仅声明获取方法。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>SPI 扩展</b></p>
 * <ul>
 *   <li>通过该接口提供链路追踪信息，便于不同链路追踪实现接入。</li>
 * </ul>
 *
 * @since 2025/9/26
 * @version 1.0
 */
public interface ICTraceInfoProvider<T extends ICTraceInfo> {

    /**
     * 获取链路追踪信息
     * @return 链路追踪信息
     */
    T getTraceInfo();

}
