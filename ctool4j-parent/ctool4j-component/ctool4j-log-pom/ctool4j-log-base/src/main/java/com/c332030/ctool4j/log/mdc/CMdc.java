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
 * @see "doc/design/log/CMdc.adoc"
 * @see "doc/design/log/CMdcTests.adoc"
 * @since 2026/8/31
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
