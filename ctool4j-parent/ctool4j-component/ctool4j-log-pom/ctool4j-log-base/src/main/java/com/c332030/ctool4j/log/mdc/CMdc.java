package com.c332030.ctool4j.log.mdc;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.c332030.ctool4j.core.util.CMapView;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Description: CMdc
 * </p>
 *
 * <p>
 * MDC 上下文存储基类，基于 {@link TransmittableThreadLocal} 承载 MDC，支持跨线程透传；
 * 供各日志后端（logback / log4j2）的 MDC 适配器继承，屏蔽存储与基础读写差异。
 * </p>
 *
 * <p>
 * 存储由 {@link CMapView} 承载，内部维护「可变实例 + 不可变视图」两实例：
 * 写操作直接改可变实例，读不可变视图零构建（视图为实时视图，底层修改自动反映）。
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code put/get/remove/clear}：操作当前线程上下文。</li>
 *   <li>{@code containsKey/isEmpty}：键判断与空判断。</li>
 *   <li>{@code getMdcMap}：获取可变实例（供子类读写）。</li>
 *   <li>{@code getImmutableMap}：获取不可变视图（零构建，供子类读不可变副本）。</li>
 * </ul>
 * <p>logback 版 {@code CMdcLogback}（实现 slf4j {@code MDCAdapter}）与 log4j2 版 {@code CMdcLog4j}（实现 log4j2 {@code ThreadContextMap}）均继承本类，各自补充后端 SPI 特有方法。</p>
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
 *     <td>put/get/remove 传入 null key/value</td>
 *     <td>依赖 ConcurrentHashMap 抛 NPE（不静默吞）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>作为各日志后端 MDC 适配器的公共父类，统一 TTL 存储与基础读写。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不直接面向业务使用，需通过各后端的 {@code CMdcLogback} / {@code CMdcLog4j} 子类间接使用。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>基于 TransmittableThreadLocal，需配合 TTL 依赖与跨线程包装使用。</li>
 *   <li>依赖 {@code ConcurrentHashMap} 的 null 限制（不允许 null key/value）。</li>
 *   <li>不可变视图为只读视图（非快照），持有方需知晓底层可变实例被修改后视图内容随之变化。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>存储结构（可变实例 + 不可变视图）</b></p>
 * <ul>
 *   <li>{@code CMapView}（core 通用容器）同时持有「可变实例」与「不可变视图」两实例：</li>
 *   <li>可变实例：{@code ConcurrentHashMap}，承载写操作（put/remove）。</li>
 *   <li>不可变视图：{@code Collections.unmodifiableMap(可变实例)}，可变实例的只读视图（live view），</li>
 *   <li>底层修改实时反映，无需每次重新构建。</li>
 *   <li>{@code TransmittableThreadLocal.withInitial(CMapView::new)} 存上下文，支持跨线程（线程池、异步任务）传递。</li>
 * </ul>
 * <p><b>抽象边界</b></p>
 * <ul>
 *   <li>本类只承载「存储 + 基础读写」通用逻辑；后端 SPI 特有语义（如 slf4j 的 {@code setContextMap}、log4j2 的 {@code getImmutableMapOrNull}）由子类实现。</li>
 * </ul>
 *
 * @since 2026/8/31
 * @version 1.0
 */
public class CMdc {

    /**
     * 线程上下文存储：同时持有可变实例与不可变视图
     * <p>MDC 场景需线程安全，可变实例显式用 ConcurrentHashMap</p>
     */
    protected static final TransmittableThreadLocal<CMapView<String, String>> MDC_CONTEXT_THREAD_LOCAL =
            TransmittableThreadLocal.withInitial(() -> CMapView.of(new ConcurrentHashMap<>()));

    /**
     * 获取当前线程上下文（可变实例）
     *
     * @return 当前线程上下文
     */
    protected Map<String, String> getMdcMap() {
        return MDC_CONTEXT_THREAD_LOCAL.get().getMutable();
    }

    /**
     * 获取当前线程上下文的不可变视图（零构建）
     *
     * @return 不可变视图
     */
    protected Map<String, String> getImmutableMap() {
        return MDC_CONTEXT_THREAD_LOCAL.get().getImmutable();
    }

    /**
     * 放入键值
     *
     * @param key 键
     * @param val 值
     */
    public void put(String key, String val) {
        getMdcMap().put(key, val);
    }

    /**
     * 获取键对应值
     *
     * @param key 键
     * @return 值
     */
    public String get(String key) {
        return getMdcMap().get(key);
    }

    /**
     * 移除键
     *
     * @param key 键
     */
    public void remove(String key) {
        getMdcMap().remove(key);
    }

    /**
     * 清空当前线程上下文
     */
    public void clear() {
        MDC_CONTEXT_THREAD_LOCAL.remove();
    }

    /**
     * 是否包含键
     *
     * @param key 键
     * @return 是否包含
     */
    public boolean containsKey(String key) {
        return getMdcMap().containsKey(key);
    }

    /**
     * 是否为空
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return getMdcMap().isEmpty();
    }

}
