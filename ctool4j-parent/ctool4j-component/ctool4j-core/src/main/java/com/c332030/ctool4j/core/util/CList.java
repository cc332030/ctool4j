package com.c332030.ctool4j.core.util;

import cn.hutool.core.util.ArrayUtil;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Description: CList
 * </p>
 *
 * @since 2024/11/12
 */
@UtilityClass
public class CList {

    public <T> List<T> of() {
        return Collections.emptyList();
    }

    public <T> List<T> of(T t) {

        if (t == null) {
            return of();
        }
        return Collections.singletonList(t);
    }

    @SafeVarargs
    public <T> List<T> of(T... ts) {

        val tsNew = CArrUtils.filterNull(ts);
        if (ArrayUtil.isEmpty(tsNew)) {
            return of();
        }

        return Collections.unmodifiableList(tsNew);
    }

}
