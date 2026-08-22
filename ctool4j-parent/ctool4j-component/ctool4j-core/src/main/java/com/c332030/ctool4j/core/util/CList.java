package com.c332030.ctool4j.core.util;

import cn.hutool.core.util.ArrayUtil;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Description: CList，{@code List.of}（JDK 9+）在低版本 JDK 的替代构造工具。
 * </p>
 *
 * <p><b>JDK 版本兼容：</b>当运行环境 JDK ≥ 9 时，优先使用 JDK 自带的 {@link List#of}；
 * 仅当 JDK 不支持（如 JDK 8 目标）时才使用本类。语义差异：{@code List.of} 不允许 null 元素
 * （抛 {@link NullPointerException}），本类自动过滤 null 元素。</p>
 *
 * @since 2024/11/12
 * @see "doc/design/core/CList.adoc"
 * @see "doc/design/core/CListTests.adoc"
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
