package com.c332030.ctool4j.web.spi;

import com.c332030.ctool4j.web.model.model.CTraceInfo;

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
