package com.c332030.ctool4j.core.util;

import java.util.Collection;

/**
 * <p>
 * Description: CCollOpt
 * </p>
 *
 * @since 2025/12/6
 */
public class CCollOpt<T> extends CIterOpt<T> {

    public static final CCollOpt<?> EMPTY = new CCollOpt<>(null);

    protected CCollOpt(Collection<T> value){
        super(value);
    }

}
