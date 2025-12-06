package com.c332030.ctool4j.core.util;

import cn.hutool.core.collection.IterUtil;
import com.c332030.ctool4j.definition.function.CConsumer;

import java.util.function.Consumer;

/**
 * <p>
 * Description: CIterOpt
 * </p>
 *
 * @since 2025/12/6
 */
public class CIterOpt<E, T extends Iterable<E>> extends COpt<T> {

    public static final CIterOpt<?, ?> EMPTY = new CIterOpt<>(null);

    protected CIterOpt(T value){
        super(value);
    }

    public boolean isEmpty() {
        return IterUtil.isEmpty(value);
    }

    public void ifNotEmpty(Consumer<T> consumer) {
        if (!isEmpty()) {
            consumer.accept(value);
        }
    }

    public void forEachIfNotEmpty(CConsumer<E> consumer) {
        ifNotEmpty(e -> e.forEach(consumer));
    }

}
