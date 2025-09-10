package com.c332030.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Description: CMap
 * </p>
 *
 * @since 2024/12/3
 */
@UtilityClass
public class CMap {

    public <K, V> Map<K, V> of() {
        return Collections.emptyMap();
    }

    public <K, V> Map<K, V> of(
            K k1, V v1
    ) {
        return Collections.singletonMap(k1, v1);
    }

    public <K, V> Map<K, V> of(
            K k1, V v1,
            K k2, V v2
    ) {
        val map = new HashMap<K, V>(2);
        map.put(k1, v1);
        map.put(k2, v2);
        return Collections.unmodifiableMap(map);
    }

    public <K, V> Map<K, V> of(
            K k1, V v1,
            K k2, V v2,
            K k3, V v3
    ) {
        val map = new HashMap<K, V>(2);
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return Collections.unmodifiableMap(map);
    }

}
