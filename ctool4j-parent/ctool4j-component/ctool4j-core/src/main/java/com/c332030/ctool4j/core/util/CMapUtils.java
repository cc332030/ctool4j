package com.c332030.ctool4j.core.util;

import cn.hutool.core.map.MapUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.definition.function.CFunction;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.*;

/**
 * <p>
 * Description: CMapUtils
 * </p>
 *
 * @since 2024/2/26
 */
@UtilityClass
public class CMapUtils {

    public <K, V> V put(Map<K, V> map, K k, V v) {
        if(Objects.isNull(map) || Objects.isNull(k) || Objects.isNull(v)) {
            return null;
        }

        return map.put(k, v);
    }

    public <K, V> Map<K, V> defaultEmpty(Map<K, V> map) {
        return MapUtil.isEmpty(map) ? Collections.emptyMap() : map;
    }

    public static final TypeReference<Map<String, Object>> MAP_STRING_OBJECT_TYPE_REFERENCE =
            new TypeReference<Map<String, Object>>() {};

    public static final TypeReference<List<Map<String, Object>>> LIST_MAP_STRING_OBJECT_TYPE_REFERENCE =
            new TypeReference<List<Map<String, Object>>>() {};

    public static final TypeReference<Map<String, String>> MAP_STRING_STRING_TYPE_REFERENCE =
            new TypeReference<Map<String, String>>() {};

    private static Map<String, String> toStringValueMap(Map<String, Object> map) {
        if(null == map) {
            return null;
        }

        val stringStringMap = new LinkedHashMap<String, String>(map.size());
        map.forEach((k, v) -> stringStringMap.put(k, Objects.toString(v, null)));
        return stringStringMap;
    }

    public <K, V> Map<K, V> newMap(K object, Integer size) {

        if(null == object) {
            throw new IllegalArgumentException("object can't be null");
        }

        return newMap(object.getClass(), size);
    }

    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> newMap(Class<?> type, Integer size) {

        if(type.isEnum()) {
            return newEnumMap((Class<Enum<?>>) type);
        }
        return new LinkedHashMap<>(size);
    }

    @SuppressWarnings({
            "unchecked",
            "rawtypes"
    })
    public <K, V> Map<K, V> newEnumMap(Class<? extends Enum<?>> type) {

        if(!type.isEnum()) {
            throw new IllegalArgumentException(type.getName() + " is not an enum");
        }

        return new EnumMap(type);
    }

    public <K1, K2, V> Map<K2, V> mapKey(
            Map<K1, V> map,
            CFunction<K1, K2> keyMapper
    ) {
        return map(map, keyMapper, CFunction.self());
    }

    public <K, V1, V2> Map<K, V2> mapValue(
            Map<K, V1> map,
            CFunction<V1, V2> valueMapper
    ) {
        return map(map, CFunction.self(), valueMapper);
    }

    public <K1, V1, K2, V2> Map<K2, V2> map(
            Map<K1, V1> map,
            CFunction<K1, K2> keyMapper,
            CFunction<V1, V2> valueMapper
    ) {

        if(MapUtil.isEmpty(map)) {
            return Collections.emptyMap();
        }

        val map2 = new LinkedHashMap<K2, V2>(map.size());
        map.forEach((k1, v1) -> {

            val k2 = CObjUtils.convert(k1, keyMapper);
            val v2 = CObjUtils.convert(v1, valueMapper);
            if(Objects.isNull(k2)
                    || Objects.isNull(v2)
            ) {
                return;
            }

            map2.put(k2, v2);

        });

        return map2;
    }

}
