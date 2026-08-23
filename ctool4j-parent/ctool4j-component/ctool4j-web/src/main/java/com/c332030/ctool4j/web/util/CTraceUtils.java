package com.c332030.ctool4j.web.util;

import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.IdUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.c332030.ctool4j.core.util.CSpiUtils;
import com.c332030.ctool4j.core.util.CStrUtils;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.model.model.ICTraceInfo;
import com.c332030.ctool4j.web.spi.CTraceInfoProvider;
import com.c332030.ctool4j.web.spi.ICTraceInfoProvider;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.slf4j.MDC;

/**
 * <p>
 * Description: CTraceUtils
 * </p>
 *
 * @since 2025/9/26
 * @see "doc/design/web/CTraceUtils.adoc"
 * @see "doc/design/web/CTraceUtilsTests.adoc"
 */
@UtilityClass
public class CTraceUtils {

    /**
     * 追踪 id 在 MDC 及请求头中的键名
     */
    public static final String TRACE_ID = "c-trace-id";

    /**
     * 追踪信息提供者，优先使用自定义实现，否则使用默认实现
     */
    @SuppressWarnings("unchecked")
    public static final ICTraceInfoProvider<ICTraceInfo> BUSINESS_EXCEPTION_PROVIDER =
            CSpiUtils.getFirstCustomImplOrDefault(ICTraceInfoProvider.class, CTraceInfoProvider.class);

    private static final TransmittableThreadLocal<ICTraceInfo> TRACE_INFO_THREAD_LOCAL =
            TransmittableThreadLocal.withInitial(BUSINESS_EXCEPTION_PROVIDER::getTraceInfo);

    /**
     * 获取当前线程的追踪信息
     *
     * @param <T> 追踪信息类型
     * @return 当前线程的追踪信息
     */
    @SuppressWarnings("unchecked")
    public <T extends ICTraceInfo> T getTraceInfo() {
        return (T)TRACE_INFO_THREAD_LOCAL.get();
    }

    /**
     * 移除当前线程的追踪信息
     */
    public void removeTraceInfo() {
        TRACE_INFO_THREAD_LOCAL.remove();
    }

    /**
     * 生成追踪 id
     *
     * @return 生成的追踪 id
     */
    public String generateTraceId() {
        return IdUtil.objectId() + "-1";
    }

    /**
     * 初始化追踪：优先取请求头中的追踪 id 自增，否则新生成
     */
    public void initTrace() {

        val traceId = Opt.ofNullable(CRequestUtils.getRequestDefaultNull())
                .map(request -> request.getHeader(TRACE_ID))
                .map(CStrUtils::incrLastNum)
                .orElseGet(CTraceUtils::generateTraceId);

        setTraceId(traceId);

    }

    /**
     * 获取当前追踪 id
     *
     * @return 当前追踪 id
     */
    public String getTraceId() {
        return getTraceInfo().getTraceId();
    }

    /**
     * 设置追踪 id 并写入 MDC
     *
     * @param traceId 追踪 id
     */
    public void setTraceId(String traceId) {
        getTraceInfo().setTraceId(traceId);
        MDC.put(TRACE_ID, traceId);
    }

    /**
     * 移除当前追踪 id 及 MDC 中的记录
     */
    public void removeTraceId() {
        getTraceInfo().setTraceId(null);
        MDC.remove(TRACE_ID);
    }

}
