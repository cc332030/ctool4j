package com.c332030.ctool4j.web.spi;

import com.c332030.ctool4j.web.model.model.ICTraceInfo;

/**
 * <p>
 * Description: ICTraceInfoProvider
 * </p>
 *
 * @since 2025/9/26
 */
public interface ICTraceInfoProvider<T extends ICTraceInfo> {

    /**
     * 获取链路追踪信息
     * @return 链路追踪信息
     */
    T getTraceInfo();

}
