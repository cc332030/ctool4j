package com.c332030.ctool.core.util;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
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
        return Collections.singletonList(t);
    }

    @SafeVarargs
    public <T> List<T> of(T... ts) {
        return Collections.unmodifiableList(Arrays.asList(ts));
    }

}
