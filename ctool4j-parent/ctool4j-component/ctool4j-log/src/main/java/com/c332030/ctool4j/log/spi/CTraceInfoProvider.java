package com.c332030.ctool4j.log.spi;

import com.c332030.ctool4j.log.model.CTraceInfo;

/**
 * <p>
 * Description: CTraceInfoProvider
 * </p>
 *
 * @since 2025/9/26
 */
public class CTraceInfoProvider implements ICTraceInfoProvider<CTraceInfo> {

    @Override
    public CTraceInfo getTraceInfo() {
        return new CTraceInfo();
    }

}
