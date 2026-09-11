package com.c332030.ctool4j.logback.mdc;

import com.c332030.ctool4j.log.mdc.CMdc;
import lombok.val;
import org.slf4j.spi.MDCAdapter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * Description: CMdcLogback
 * </p>
 *
 * <p>
 * logback 后端的 MDC 适配器，实现 slf4j {@link MDCAdapter}；
 * 存储与基础读写逻辑见父类 {@link CMdc}，此处仅补充 slf4j 特有语义。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CMdcLogback} 实现 SLF4J {@code MDCAdapter}，继承公共存储基类 {@code CMdc}，实现日志上下文在线程间传递。</p>
 * <ul>
 *   <li>基础读写（{@code put/get/remove/clear}、{@code containsKey/isEmpty}）：继承自 {@code CMdc}。</li>
 *   <li>{@code getCopyOfContextMap}：返回上下文副本（{@code LinkedHashMap}）。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>setContextMap(null)</td>
 *     <td>清空上下文</td>
 *   </tr>
 *   <tr>
 *     <td>setContextMap(普通 Map)</td>
 *     <td>清空后 putAll 拷贝</td>
 *   </tr>
 *   <tr>
 *     <td>getCopyOfContextMap</td>
 *     <td>返回新 LinkedHashMap 副本，修改不影响原 MDC</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要跨线程传递 MDC 上下文（异步任务、线程池）的 logback 日志场景。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>{@code ConcurrentHashMap} 不允许 null key/value，put(null, val) 等会抛 NPE。</li>
 *   <li>与 log4j2 版 {@code CMdcLog4j}（{@code com.c332030.ctool4j.log4j.mdc}）互斥使用。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>基于 TransmittableThreadLocal，需配合 TTL 依赖与跨线程包装使用。</li>
 *   <li>依赖 {@code ConcurrentHashMap} 的 null 限制。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>存储结构</b></p>
 * <ul>
 *   <li>存储与基础读写下沉至父类 {@code CMdc}（可变实例 + 不可变视图），本类只补充 slf4j 特有语义。</li>
 * </ul>
 *
 * @since 2025/9/26
 * @version 1.0
 */
public class CMdcLogback extends CMdc implements MDCAdapter {

    /**
     * 获取上下文副本
     *
     * @return 上下文副本
     */
    @Override
    public Map<String, String> getCopyOfContextMap() {
        return new LinkedHashMap<>(getMdcMap());
    }

    /**
     * 设置上下文，null 时清空
     *
     * <h2>setContextMap</h2>
     * <ul>
     *   <li>清空可变实例后 {@code putAll} 拷贝传入内容（null 时仅清空）；符合 slf4j「拷贝传入 map」语义。</li>
     * </ul>
     * <ul>
     *   <li>{@code setContextMap(Map)}：设置整个上下文，null 时清空。</li>
     * </ul>
     *
     * @param contextMap 上下文*/
    @Override
    public void setContextMap(Map<String, String> contextMap) {

        val map = getMdcMap();
        map.clear();

        if(contextMap != null) {
            map.putAll(contextMap);
        }
    }

}
