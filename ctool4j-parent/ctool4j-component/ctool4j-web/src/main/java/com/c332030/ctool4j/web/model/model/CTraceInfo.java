package com.c332030.ctool4j.web.model.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: CTraceInfo
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CTraceInfo} 为跟踪信息实体，实现 {@code ICTraceInfo}，含 {@code traceId} 字段。</p>
 * <p>标注 {@code @Data}/{@code @SuperBuilder}/{@code @NoArgsConstructor}/{@code @AllArgsConstructor}。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>请求链路跟踪信息（traceId）的默认实现。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>仅含 traceId 单字段，可扩展。</li>
 * </ul>
 *
 * @since 2025/9/26
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CTraceInfo implements ICTraceInfo {

    /**
     * 跟踪id
     */
    String traceId;

}
