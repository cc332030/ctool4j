package com.c332030.ctool4j.core.util;

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
 * @see "doc/design/core/CSet.adoc"
 * @see "doc/design/core/CSetTests.adoc"
 */
@UtilityClass
public class CSet {

    /**
     * 获取空 Set
     *
     * @param <T> 元素类型
     * @return 空 Set
     */
    public <T> Set<T> of() {
        return Collections.emptySet();
    }

    /**
     * 获取元素 Set（过滤 null 元素）
     *
     * @param ts  元素
     * @param <T> 元素类型
     * @return 不可变 Set
     */
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

    /**
     * 获取枚举 Set（过滤 null 元素）
     *
     * @param ts  枚举元素
     * @param <T> 枚举类型
     * @return 不可变枚举 Set
     */
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
