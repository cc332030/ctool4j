package com.c332030.ctool4j.web.model.model;

/**
 * <p>
 * Description: ICTraceInfo
 * </p>
 *
 * @since 2025/9/26
 * @see "doc/design/web/ICTraceInfo.adoc"
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
