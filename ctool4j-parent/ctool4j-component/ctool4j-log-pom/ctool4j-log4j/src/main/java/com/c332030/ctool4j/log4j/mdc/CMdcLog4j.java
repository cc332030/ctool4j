package com.c332030.ctool4j.log4j.mdc;

import com.c332030.ctool4j.log.mdc.CMdc;
import org.apache.logging.log4j.spi.ThreadContextMap;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Description: CMdcLog4j
 * </p>
 *
 * <p>
 * log4j2 后端的 MDC 适配器，实现 log4j2 {@link ThreadContextMap}；
 * 存储与基础读写逻辑见父类 {@link CMdc}，此处仅补充 log4j2 特有语义。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CMdcLog4j} 实现 log4j2 {@code ThreadContextMap}，继承公共存储基类 {@code CMdc}，实现日志上下文在线程间传递。</p>
 * <ul>
 *   <li>基础读写（{@code put/get/remove/clear}、{@code containsKey/isEmpty}）：继承自 {@code CMdc}。</li>
 *   <li>{@code getCopy}：返回上下文副本（{@code HashMap}）。</li>
 *   <li>{@code getImmutableMapOrNull}：返回不可变上下文，空时返回 null（零构建，直接返回父类不可变视图）。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>clear</td>
 *     <td>移除当前线程上下文</td>
 *   </tr>
 *   <tr>
 *     <td>getImmutableMapOrNull</td>
 *     <td>空时返回 null；非空返回不可变视图</td>
 *   </tr>
 *   <tr>
 *     <td>getCopy</td>
 *     <td>返回新 HashMap 副本，修改不影响原 MDC</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要跨线程传递 MDC 上下文（异步任务、线程池）的 log4j2 日志场景。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>{@code ConcurrentHashMap} 不允许 null key/value，put(null, val) 等会抛 NPE。</li>
 *   <li>与 logback 版 {@code CMdcLogback}（{@code com.c332030.ctool4j.logback.mdc}）互斥使用。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>基于 TransmittableThreadLocal，需配合 TTL 依赖与跨线程包装使用。</li>
 *   <li>依赖 {@code ConcurrentHashMap} 的 null 限制。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>存储结构</b></p>
 * <ul>
 *   <li>存储与基础读写下沉至父类 {@code CMdc}（可变实例 + 不可变视图），本类只补充 log4j2 特有语义。</li>
 * </ul>
 * <p><b>接口语义</b></p>
 * <ul>
 *   <li>实现 log4j2 {@code ThreadContextMap}，作为 log4j2 {@code ThreadContext} 的底层存储，替换默认 InheritableThreadLocal 实现以支持线程池透传。</li>
 *   <li>{@code getImmutableMapOrNull} 为日志输出热点，直接返回父类不可变视图（零构建），避免每次复制。</li>
 * </ul>
 *
 * @since 2026/8/31
 * @version 1.0
 */
public class CMdcLog4j extends CMdc implements ThreadContextMap {

    /**
     * 获取上下文副本
     *
     * @return 上下文副本
     */
    @Override
    public Map<String, String> getCopy() {
        return new HashMap<>(getMdcMap());
    }

    /**
     * 获取不可变上下文或 null
     *
     * @return 不可变上下文，空时返回 null
     */
    @Override
    public Map<String, String> getImmutableMapOrNull() {
        if(isEmpty()) {
            return null;
        }
        return getImmutableMap();
    }

}
