package com.c332030.ctool4j.core.util;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.definition.function.CConsumer;

/**
 * <p>
 * Description: CMapOpt
 * </p>
 *
 * @since 2025/12/6
 */
@Deprecated
public class CStrOpt<T extends CharSequence> extends COpt<T> {

    public static final CStrOpt<?> EMPTY = new CStrOpt<>(null);

    protected CStrOpt(T value){
        super(value);
    }

    public boolean isEmpty() {
        return StrUtil.isEmpty(value);
    }

    public boolean isBlank() {
        return StrUtil.isBlank(value);
    }

    public void ifNotEmpty(CConsumer<T> consumer) {
        if (!isEmpty()) {
            consumer.accept(value);
        }
    }

    public void ifNotBlank(CConsumer<T> consumer) {
        if (!isEmpty()) {
            consumer.accept(value);
        }
    }

}
