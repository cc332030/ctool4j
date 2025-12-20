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
@Deprecated
public class CIterOpt<T> extends COpt<Iterable<T>> {

    public static final CIterOpt<?> EMPTY = new CIterOpt<>(null);

    protected CIterOpt(Iterable<T> value){
        super(value);
    }

    public boolean isEmpty() {
        return IterUtil.isEmpty(value);
    }

    public void ifNotEmpty(Consumer<Iterable<T>> consumer) {
        if (!isEmpty()) {
            consumer.accept(value);
        }
    }

    public void forEachIfNotEmpty(CConsumer<T> consumer) {
        ifNotEmpty(e -> e.forEach(consumer));
    }

}
