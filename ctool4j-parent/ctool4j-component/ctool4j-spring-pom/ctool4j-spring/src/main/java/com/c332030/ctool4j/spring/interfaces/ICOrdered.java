package com.c332030.ctool4j.spring.interfaces;

import org.springframework.core.Ordered;

/**
 * <p>
 * Description: ICOrdered
 * </p>
 *
 * @since 2025/9/28
 */
public interface ICOrdered<T extends ICOrdered<T>> extends Ordered, Comparable<T> {

    /**
     * 获取排序值
     * @return 排序值
     */
    @Override
    default int getOrder() {
        return 0;
    }

    /**
     * 按排序值比较
     * @param o 待比较对象
     * @return 比较结果
     */
    @Override
    default int compareTo(T o) {
        return Integer.compare(this.getOrder(), o.getOrder());
    }

}
