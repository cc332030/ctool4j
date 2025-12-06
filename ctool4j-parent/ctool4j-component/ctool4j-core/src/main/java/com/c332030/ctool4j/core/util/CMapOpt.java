package com.c332030.ctool4j.core.util;

import cn.hutool.core.map.MapUtil;
import com.c332030.ctool4j.definition.function.CBiConsumer;

import java.util.Map;
import java.util.function.Consumer;

/**
 * <p>
 * Description: CMapOpt
 * </p>
 *
 * @since 2025/12/6
 */
public class CMapOpt<K, V, T extends Map<K, V>> extends COpt<T> {

    public static final CMapOpt<?, ?, ?> EMPTY = new CMapOpt<>(null);

    protected CMapOpt(T value){
        super(value);
    }

    public boolean isEmpty() {
        return MapUtil.isEmpty(value);
    }

    public void ifNotEmpty(Consumer<T> consumer) {
        if (!isEmpty()) {
            consumer.accept(value);
        }
    }

    public void forEachIfNotEmpty(CBiConsumer<K, V> consumer) {
        ifNotEmpty(e -> e.forEach(consumer));
    }

}
