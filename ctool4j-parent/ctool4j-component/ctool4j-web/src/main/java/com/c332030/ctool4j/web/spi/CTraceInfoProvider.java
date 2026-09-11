package com.c332030.ctool4j.web.spi;

import com.c332030.ctool4j.web.model.model.CTraceInfo;

/**
 * <p>
 * Description: CTraceInfoProvider
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CTraceInfoProvider} 为默认追踪信息提供者，实现 {@code ICTraceInfoProvider&lt;CTraceInfo&gt;}，{@code getTraceInfo()} 返回新的 {@code CTraceInfo} 实例。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>作为 CTraceUtils 的默认追踪信息提供者（无自定义 SPI 时）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>返回空 traceId 的 CTraceInfo，由调用方后续设置。</li>
 * </ul>
 *
 * @since 2025/9/26
 * @version 1.0
 */
public class CTraceInfoProvider implements ICTraceInfoProvider<CTraceInfo> {

    /**
     * 获取默认跟踪信息
     *
     * @return 跟踪信息
     */
    @Override
    public CTraceInfo getTraceInfo() {
        return new CTraceInfo();
    }

}
