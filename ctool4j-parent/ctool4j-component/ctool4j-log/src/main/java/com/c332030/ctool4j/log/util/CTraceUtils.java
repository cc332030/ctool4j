package com.c332030.ctool4j.log.util;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.c332030.ctool4j.core.util.CSpiUtils;
import com.c332030.ctool4j.log.model.ITraceInfo;
import com.c332030.ctool4j.log.spi.CTraceInfoProvider;
import com.c332030.ctool4j.log.spi.ITraceInfoProvider;
import lombok.experimental.UtilityClass;
import org.slf4j.MDC;

/**
 * <p>
 * Description: CTraceUtils
 * </p>
 *
 * @since 2025/9/26
 */
@UtilityClass
public class CTraceUtils {

    public static final String TRACE_ID = "traceId";

    @SuppressWarnings("unchecked")
    public static final ITraceInfoProvider<ITraceInfo> BUSINESS_EXCEPTION_PROVIDER =
            CSpiUtils.getFirstCustomImplOrDefault(ITraceInfoProvider.class, CTraceInfoProvider.class);

    private static final TransmittableThreadLocal<ITraceInfo> TRACE_INFO_THREAD_LOCAL =
            TransmittableThreadLocal.withInitial(BUSINESS_EXCEPTION_PROVIDER::getTraceInfo);

    @SuppressWarnings("unchecked")
    public <T extends ITraceInfo> T getTraceInfo() {
        return (T)TRACE_INFO_THREAD_LOCAL.get();
    }

    public void removeTraceInfo() {
        TRACE_INFO_THREAD_LOCAL.remove();
    }

    public String getTraceId() {
        return getTraceInfo().getTraceId();
    }

    public void setTraceId(String traceId) {
        getTraceInfo().setTraceId(traceId);
        MDC.put(TRACE_ID, traceId);
    }

    public void removeTraceId() {
        getTraceInfo().setTraceId(null);
        MDC.remove(TRACE_ID);
    }

}
