package com.c332030.ctool4j.core.util;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.definition.function.CBiPredicate;
import com.c332030.ctool4j.definition.function.CFunction;
import com.c332030.ctool4j.definition.function.CPredicate;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CMapUtils
 * </p>
 *
 * @since 2024/2/26
 */
@UtilityClass
public class CMapUtils {

    /**
     * 设置 map 值
     *
     * @param map Map
     * @param k   键
     * @param v   值
     * @param <K> 键泛型
     * @param <V> 值泛型
     * @return 值
     */
    public <K, V> V put(Map<K, V> map, K k, V v) {
        if (Objects.isNull(map) || Objects.isNull(k) || Objects.isNull(v)) {
            return null;
        }

        return map.put(k, v);
    }

    /**
     * 默认空 map
     *
     * @param map Map
     * @param <K> 键泛型
     * @param <V> 值泛型
     * @return 原 Map 或 空 Map
     */
    public <K, V> Map<K, V> defaultEmpty(Map<K, V> map) {
        return MapUtil.isEmpty(map) ? Collections.emptyMap() : map;
    }

    /**
     * Map[String, Object] 映射
     */
    public final TypeReference<Map<String, Object>> MAP_STRING_OBJECT_TYPE_REFERENCE =
        new TypeReference<Map<String, Object>>() {
        };

    /**
     * List[Map[String, Object]]映射
     */
    public final TypeReference<List<Map<String, Object>>> LIST_MAP_STRING_OBJECT_TYPE_REFERENCE =
        new TypeReference<List<Map<String, Object>>>() {
        };

    /**
     * Map[String, String] 映射
     */
    public final TypeReference<Map<String, String>> MAP_STRING_STRING_TYPE_REFERENCE =
        new TypeReference<Map<String, String>>() {
        };

    /**
     * 将 Map[String, Object] 转换为 Map[String, String]
     *
     * @param map Map[String, Object]
     * @return Map[String, String]
     */
    public Map<String, String> toStringValueMap(Map<String, Object> map) {

        if (null == map) {
            return null;
        }

        val stringStringMap = new LinkedHashMap<String, String>(map.size());
        map.forEach((k, v) -> stringStringMap.put(k, Objects.toString(v, null)));
        return stringStringMap;
    }

    /**
     * 创建 Map
     *
     * @param object 键值（任意一个）
     * @param size   大小
     * @param <K>    键泛型
     * @param <V>    值泛型
     * @return Map
     */
    public <K, V> Map<K, V> newMap(K object, Integer size) {

        if (null == object) {
            throw new IllegalArgumentException("object can't be null");
        }

        return newMap(object.getClass(), size);
    }

    /**
     * 创建 Map
     *
     * @param type 键类
     * @param size 大小
     * @param <K>  键泛型
     * @param <V>  值泛型
     * @return Map
     */
    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> newMap(Class<?> type, Integer size) {

        if (type.isEnum()) {
            return newEnumMap((Class<Enum<?>>) type);
        }
        return new LinkedHashMap<>(size);
    }

    /**
     * 创建 EnumMap
     *
     * @param type 枚举类
     * @param <K>  键泛型
     * @param <V>  值泛型
     * @return Map
     */
    @SuppressWarnings({
        "unchecked",
        "rawtypes"
    })
    public <K, V> Map<K, V> newEnumMap(Class<? extends Enum<?>> type) {

        if (!type.isEnum()) {
            throw new IllegalArgumentException(type.getName() + " is not an enum");
        }

        return new EnumMap(type);
    }

    /**
     * 创建忽略大小写排序的 Map
     *
     * @param <V> 值泛型
     * @return Map
     */
    public <V> TreeMap<String, V> newIgnoreCaseMap() {
        return new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    /**
     * Map 键映射
     *
     * @param map       原 Map
     * @param keyMapper 键映射
     * @param <K1>      原键泛型
     * @param <K2>      新键泛型
     * @param <V>       值泛型
     * @return 新 Map
     */
    public <K1, K2, V> Map<K2, V> mapKey(
        Map<K1, V> map,
        CFunction<K1, K2> keyMapper
    ) {
        return map(map, keyMapper, CFunction.self());
    }

    /**
     * Map 值映射
     *
     * @param map         原 Map
     * @param valueMapper 值映射
     * @param <K>         键泛型
     * @param <V1>        原值泛型
     * @param <V2>        新值泛型
     * @return 新 Map
     */
    public <K, V1, V2> Map<K, V2> mapValue(
        Map<K, V1> map,
        CFunction<V1, V2> valueMapper
    ) {
        return map(map, CFunction.self(), valueMapper);
    }

    /**
     * Map 键值映射
     *
     * @param map         原 Map
     * @param keyMapper   键映射
     * @param valueMapper 值映射
     * @param <K1>        原键泛型
     * @param <V1>        原值泛型
     * @param <K2>        新键泛型
     * @param <V2>        新值泛型
     * @return 新 Map
     */
    public <K1, V1, K2, V2> Map<K2, V2> map(
        Map<K1, V1> map,
        CFunction<K1, K2> keyMapper,
        CFunction<V1, V2> valueMapper
    ) {

        if (MapUtil.isEmpty(map)) {
            return Collections.emptyMap();
        }

        val map2 = new LinkedHashMap<K2, V2>(map.size());
        map.forEach((k1, v1) -> {

            val k2 = CObjUtils.convert(k1, keyMapper);
            val v2 = CObjUtils.convert(v1, valueMapper);
            if (Objects.isNull(k2)
                || Objects.isNull(v2)
            ) {
                return;
            }

            map2.put(k2, v2);

        });

        return map2;
    }

    public <K, V> Map<K, V> filter(Map<K, V> map, CBiPredicate<K, V> predicate) {
        if (MapUtil.isEmpty(map)) {
            return CMap.of();
        }

        return map.entrySet().stream()
            .filter(entry -> predicate.test(entry.getKey(), entry.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public <K, V> Map<K, V> filterKey(Map<K, V> map, CPredicate<K> predicate) {
        return filter(map, (k, v) -> predicate.test(k));
    }

    public <K, V> Map<K, V> filterValue(Map<K, V> map, CPredicate<V> predicate) {
        return filter(map, (k, v) -> predicate.test(v));
    }

    @SafeVarargs
    public <K, V> Map<K, V> merge(Map<K, V>... maps) {
        return merge(
            (e1, e2) -> e1,
            maps
        );
    }

    public Map<String, String> toAvailableStrMap(Map<String, String> map) {
        return map(
            map,
            CStrUtils::toAvailable,
            CStrUtils::toAvailable
        );
    }

    @SafeVarargs
    public <K, V> Map<K, V> merge(BinaryOperator<V> mergeFunction, Map<K, V>... maps) {

        if (ArrayUtil.isEmpty(maps)) {
            return CMap.of();
        }

        return Arrays.stream(maps)
            .filter(MapUtil::isNotEmpty)
            .map(Map::entrySet)
            .flatMap(Collection::stream)
            .filter(entry -> Objects.nonNull(entry.getValue()))
            .collect(CCollectors.toUnmodifiableLinkedMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                mergeFunction
            ));
    }

}
