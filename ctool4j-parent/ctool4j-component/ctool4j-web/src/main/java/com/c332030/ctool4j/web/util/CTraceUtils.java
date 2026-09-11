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
 * <h2>能力目录</h2>
 * <p>{@code CTraceUtils} 为链路追踪工具类，提供：</p>
 * <ul>
 *   <li>{@code TRACE_ID}：追踪 id 在 MDC 及请求头中的键名（"c-trace-id"）</li>
 *   <li>{@code getTraceInfo()} / {@code removeTraceInfo()}：获取/移除当前线程追踪信息（TransmittableThreadLocal）</li>
 *   <li>{@code getTraceId()} / {@code setTraceId()} / {@code removeTraceId()}：追踪 id 获取/设置（写 MDC）/移除</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>请求头无追踪 id</td>
 *     <td>新生成追踪 id</td>
 *   </tr>
 *   <tr>
 *     <td>请求头追踪 id</td>
 *     <td>自增后使用</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>请求链路追踪 id 的生成、传递与 MDC 关联。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>基于 TransmittableThreadLocal 支持线程池传递，依赖 TTL 库。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>提供者</b></p>
 * <ul>
 *   <li>追踪信息提供者优先取自定义 SPI 实现，否则默认 {@code CTraceInfoProvider}。</li>
 * </ul>
 * <p><b>线程上下文</b></p>
 * <ul>
 *   <li>用 {@code TransmittableThreadLocal} 保存当前线程追踪信息（支持线程池传递）。</li>
 * </ul>
 * <p><b>初始化</b></p>
 * <ul>
 *   <li>{@code initTrace} 优先取请求头追踪 id 并自增（{@code incrLastNum}），否则新生成，再 {@code setTraceId}。</li>
 * </ul>
 *
 * @since 2025/9/26
 * @version 1.0
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
     * <ul>
     *   <li>{@code generateTraceId()}：生成追踪 id（objectId + "-1"）</li>
     * </ul>
     *
     * @return 生成的追踪 id
     */
    public String generateTraceId() {
        return IdUtil.objectId() + "-1";
    }

    /**
     * 初始化追踪：优先取请求头中的追踪 id 自增，否则新生成
     *
     * <ul>
     *   <li>{@code initTrace()}：初始化追踪（优先取请求头追踪 id 自增，否则新生成）</li>
     * </ul>
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
