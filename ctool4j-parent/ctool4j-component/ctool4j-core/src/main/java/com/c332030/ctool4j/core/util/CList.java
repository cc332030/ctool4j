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
 * @see doc/design/core/CList.adoc
 * @see doc/design/core/CListTests.adoc
 */
@UtilityClass
public class CList {

    /**
     * 获取空 List
     *
     * @param <T> 元素类型
     * @return 空 List
     */
    public <T> List<T> of() {
        return Collections.emptyList();
    }

    /**
     * 获取单元素 List
     *
     * @param t   元素
     * @param <T> 元素类型
     * @return 单元素 List，元素为 null 时返回空 List
     */
    public <T> List<T> of(T t) {

        if (t == null) {
            return of();
        }
        return Collections.singletonList(t);
    }

    /**
     * 获取元素 List（过滤 null 元素）
     *
     * @param ts  元素
     * @param <T> 元素类型
     * @return 不可变 List
     */
    @SafeVarargs
    public <T> List<T> of(T... ts) {

        val tsNew = CArrUtils.filterNull(ts);
        if (ArrayUtil.isEmpty(tsNew)) {
            return of();
        }

        return Collections.unmodifiableList(tsNew);
    }

}
