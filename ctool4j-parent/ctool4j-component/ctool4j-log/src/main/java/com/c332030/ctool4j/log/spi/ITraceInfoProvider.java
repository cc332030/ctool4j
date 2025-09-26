package com.c332030.ctool4j.log.spi;

import com.c332030.ctool4j.log.model.ITraceInfo;

/**
 * <p>
 * Description: ITraceInfoProvider
 * </p>
 *
 * @since 2025/9/26
 */
public interface ITraceInfoProvider<T extends ITraceInfo> {

    T getTraceInfo();

}
