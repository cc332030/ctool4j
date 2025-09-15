package com.c332030.ctool.core.util;

import cn.hutool.core.util.ArrayUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.*;

/**
 * <p>
 * Description: CSet
 * </p>
 *
 * @since 2024/11/12
 */
@UtilityClass
public class CSet {

    public <T> Set<T> of() {
        return Collections.emptySet();
    }

    @SafeVarargs
    public <T> Set<T> of(T... ts) {

        val set = new HashSet<T>(ts.length);
        Collections.addAll(set, ts);
        return Collections.unmodifiableSet(set);
    }

    @SafeVarargs
    public <T extends Enum<T>> Set<T> of(T... ts) {

        val set = EnumSet.copyOf(Arrays.asList(ts));
        return Collections.unmodifiableSet(set);
    }

}
