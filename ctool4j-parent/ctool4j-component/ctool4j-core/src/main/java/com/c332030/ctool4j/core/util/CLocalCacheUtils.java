package com.c332030.ctool4j.core.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.experimental.UtilityClass;

/**
 * <p>
 * Description: CLocalCacheUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CLocalCacheUtils} 为本地缓存工具类，基于 Caffeine 提供：</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>内存不足</td>
 *     <td>软引用值被 JVM 回收（softValues）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要可被内存压力回收的本地缓存（值可重建的缓存场景）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>值必须常驻（不可回收）时不适用；需显式 TTL/容量策略时用原生 Caffeine 构建器配置。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>采用 softValues 而非强引用，牺牲缓存命中稳定性换取内存安全；值重建成本高时不建议。</li>
 *   <li>未配置 TTL/最大容量，仅做基础内存友好缓存；复杂策略由调用方扩展构建器。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>内存友好</b></p>
 * <ul>
 *   <li>构建器配置 {@code softValues()}，缓存值使用软引用，内存不足时 JVM 可回收缓存值，避免缓存导致 OOM。</li>
 * </ul>
 *
 * @since 2026/6/17
 * @version 1.0
 */
@UtilityClass
public class CLocalCacheUtils {

    /**
     * 获取本地缓存构建器（内存不足时释放缓存）
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 缓存构建器
     */
    @SuppressWarnings("unchecked")
    public <K, V> Caffeine<K, V> cacheBuilder() {
        return (Caffeine<K, V>)Caffeine.newBuilder()
            // 内存不足释放缓存
            .softValues()
            ;
    }

    /**
     * 构建本地缓存
     * <ul>
     *   <li>{@code buildCache()}：直接构建本地缓存</li>
     * </ul>
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 本地缓存
     */
    public <K, V> Cache<K, V> buildCache() {
        return cacheBuilder()
            .build();
    }

}
