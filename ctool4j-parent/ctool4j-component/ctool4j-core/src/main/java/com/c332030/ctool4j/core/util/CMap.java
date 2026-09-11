package com.c332030.ctool4j.core.util;

import cn.hutool.core.map.MapUtil;
import com.c332030.ctool4j.definition.function.CSupplier;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * Description: CMap，{@code Map.of}（JDK 9+）在低版本 JDK 的替代构造工具。
 * </p>
 *
 * <p><b>JDK 版本兼容：</b>当运行环境 JDK ≥ 9 时，优先使用 JDK 自带的 {@code Map.of}；
 * 仅当 JDK 不支持（如 JDK 8 目标）时才使用本类。语义差异：{@code Map.of} 不允许 null 键/值
 * （抛 {@link NullPointerException}），本类不做此限制；本类额外提供复制 Map、指定容器类型能力。</p>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>of() 空参</td>
 *     <td>返回空 Map</td>
 *   </tr>
 *   <tr>
 *     <td>of(Map) 原 Map 为空/null</td>
 *     <td>返回空 Map</td>
 *   </tr>
 *   <tr>
 *     <td>of(Map) 复制结果被修改</td>
 *     <td>抛 UnsupportedOperationException（不可变）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要不可变 Map、且自动判空的 Map 构造。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>返回不可变 Map，需要后续增删时用原生 {@code new HashMap}。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>自动判空并返回不可变副本，牺牲可修改性换取安全的 Map 构造。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>不可变语义</b></p>
 * <ul>
 *   <li>单键值 {@code of} 用 {@code Collections.singletonMap}（不可变）。</li>
 *   <li>双/三键值用 {@code HashMap.put} 后包 {@code Collections.unmodifiableMap}。</li>
 *   <li>{@code putAll} 后包不可变，不修改原 Map。</li>
 * </ul>
 *
 * @since 2024/12/3
 * @version 1.0
 */
@UtilityClass
public class CMap {

    /**
     * 获取空 Map
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 空 Map
     */
    public <K, V> Map<K, V> of() {
        return Collections.emptyMap();
    }

    /**
     * 获取单键值 Map
     *
     * @param k1  键
     * @param v1  值
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 单键值 Map
     */
    public <K, V> Map<K, V> of(
            K k1, V v1
    ) {
        return Collections.singletonMap(k1, v1);
    }

    /**
     * 获取双键值 Map
     *
     * @param k1  第一个键
     * @param v1  第一个值
     * @param k2  第二个键
     * @param v2  第二个值
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 不可变 Map
     */
    public <K, V> Map<K, V> of(
            K k1, V v1,
            K k2, V v2
    ) {
        val map = new HashMap<K, V>(2);
        map.put(k1, v1);
        map.put(k2, v2);
        return Collections.unmodifiableMap(map);
    }

    /**
     * 获取三键值 Map
     *
     * @param k1  第一个键
     * @param v1  第一个值
     * @param k2  第二个键
     * @param v2  第二个值
     * @param k3  第三个键
     * @param v3  第三个值
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 不可变 Map
     */
    public <K, V> Map<K, V> of(
            K k1, V v1,
            K k2, V v2,
            K k3, V v3
    ) {
        val map = new HashMap<K, V>(3);
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return Collections.unmodifiableMap(map);
    }

    /**
     * 复制 Map 到新有序 Map
     *
     * @param map  原 Map
     * @param <K>  键类型
     * @param <V>  值类型
     * @return 新有序 Map，原 Map 为空时返回空 Map
     */
    public <K, V> Map<K, V> of(Map<K, V> map) {
        return of(map, LinkedHashMap::new);
    }

    /**
     * 复制 Map 到指定类型新 Map
     *
     * @param map      原 Map
     * @param supplier 新 Map 供应商
     * @param <K>      键类型
     * @param <V>      值类型
     * @return 新 Map，原 Map 为空时返回空 Map
     */
    public <K, V> Map<K, V> of(Map<K, V> map, CSupplier<Map<K, V>> supplier) {

        if(MapUtil.isEmpty(map)) {
            return of();
        }

        val mapNew = supplier.get();
        mapNew.putAll(map);
        return Collections.unmodifiableMap(mapNew);
    }

}
