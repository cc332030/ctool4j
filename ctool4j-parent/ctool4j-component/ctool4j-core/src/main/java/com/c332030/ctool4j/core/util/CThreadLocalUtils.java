package com.c332030.ctool4j.core.util;

import cn.hutool.core.util.ObjUtil;
import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

/**
 * <p>
 * Description: CThreadLocalUtils
 * </p>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>getThenRemove：值为 null</td>
 *     <td>返回 null（并移除，无副作用）</td>
 *   </tr>
 *   <tr>
 *     <td>getOrDefault：值为 null</td>
 *     <td>返回默认值 / 经 supplier 获取</td>
 *   </tr>
 *   <tr>
 *     <td>getOrDefault(supplier)：值非 null</td>
 *     <td>不调用 supplier</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>一次性读取 ThreadLocal 并立即清理，防止线程池场景下的值泄漏。</li>
 *   <li>需要 ThreadLocal 缺省值语义。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要保留 ThreadLocal 值跨多次读取时不适用 getThenRemove。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>getThenRemove 优先保证不泄漏，牺牲值的持久性。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>取后移除</b></p>
 * <ul>
 *   <li>{@code getThenRemove} 在 {@code finally} 中调用 {@code threadLocal.remove()}，保证无论取值是否成功都会移除，</li>
 *   <li>避免 ThreadLocal 值泄漏（线程池复用场景尤为重要）。</li>
 * </ul>
 * <p><b>默认值</b></p>
 * <ul>
 *   <li>{@code getOrDefault} 基于 {@code ObjUtil.defaultIfNull(threadLocal.get(), ...)}：值为 null 时返回默认值。</li>
 *   <li>supplier 重载仅值为 null 时才调用 supplier（惰性）。</li>
 * </ul>
 *
 * @since 2025/9/21
 * @version 1.0
 */
@UtilityClass
public class CThreadLocalUtils {

    /**
     * 获取 ThreadLocal 值并立即移除
     * <ul>
     *   <li>{@code getThenRemove(ThreadLocal)}：获取值并立即移除</li>
     * </ul>
     *
     * @param threadLocal ThreadLocal
     * @param <T>         值类型
     * @return ThreadLocal 值
     */
    public <T> T getThenRemove(ThreadLocal<T> threadLocal) {
        try {
            return threadLocal.get();
        } finally {
            threadLocal.remove();
        }
    }

    /**
     * 获取 ThreadLocal 值，为 null 时返回默认值
     *
     * @param threadLocal  ThreadLocal
     * @param defaultValue 默认值
     * @param <T>          值类型
     * @return ThreadLocal 值或默认值
     */
    public <T> T getOrDefault(ThreadLocal<T> threadLocal, T defaultValue) {
        return ObjUtil.defaultIfNull(threadLocal.get(), defaultValue);
    }

    /**
     * 获取 ThreadLocal 值，为 null 时通过供应商获取默认值
     *
     * @param threadLocal          ThreadLocal
     * @param defaultValueSupplier 默认值供应商
     * @param <T>                  值类型
     * @return ThreadLocal 值或默认值
     */
    public <T> T getOrDefault(ThreadLocal<T> threadLocal, Supplier<T> defaultValueSupplier) {
        return ObjUtil.defaultIfNull(threadLocal.get(), defaultValueSupplier);
    }

}
