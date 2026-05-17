package com.c332030.ctool4j.log.spi;

import com.c332030.ctool4j.log.model.ICTraceInfo;

/**
 * <p>
 * Description: ICTraceInfoProvider
 * </p>
 *
 * @since 2025/9/26
 */
public interface ICTraceInfoProvider<T extends ICTraceInfo> {

    T getTraceInfo();

}
