package com.c332030.ctool4j.log.util;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.c332030.ctool4j.log.model.CTraceInfo;
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

    private static final TransmittableThreadLocal<CTraceInfo> TRACE_INFO_THREAD_LOCAL =
            TransmittableThreadLocal.withInitial(CTraceInfo::new);

    public CTraceInfo getTraceInfo() {
        return TRACE_INFO_THREAD_LOCAL.get();
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
