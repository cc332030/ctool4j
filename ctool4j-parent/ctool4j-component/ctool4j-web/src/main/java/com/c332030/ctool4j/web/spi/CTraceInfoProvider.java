package com.c332030.ctool4j.web.spi;

import com.c332030.ctool4j.web.model.model.CTraceInfo;

/**
 * <p>
 * Description: CTraceInfoProvider
 * </p>
 *
 * @since 2025/9/26
 * @see "doc/design/web/CTraceInfoProvider.adoc"
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
