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
 * Description: CMap
 * </p>
 *
 * @since 2024/12/3
 * @see doc/design/core/CMap.adoc
 * @see doc/design/core/CMapTests.adoc
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
