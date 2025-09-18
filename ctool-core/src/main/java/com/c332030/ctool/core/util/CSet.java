package com.c332030.ctool.core.util;

import cn.hutool.core.util.ArrayUtil;
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

        val tsNew = CArrUtils.filterNull(ts);
        if(ArrayUtil.isEmpty(tsNew)) {
            return of();
        }

        val set = new HashSet<T>(tsNew.size());
        set.addAll(tsNew);
        return Collections.unmodifiableSet(set);
    }

    @SafeVarargs
    public <T extends Enum<T>> Set<T> of(T... ts) {

        val tsNew = CArrUtils.filterNull(ts);
        if(ArrayUtil.isEmpty(tsNew)) {
            return of();
        }

        val set = EnumSet.copyOf(tsNew);
        return Collections.unmodifiableSet(set);
    }

}
