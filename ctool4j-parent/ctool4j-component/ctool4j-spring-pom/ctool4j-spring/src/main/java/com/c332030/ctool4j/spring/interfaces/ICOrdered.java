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

    @Override
    default int getOrder() {
        return 0;
    }

    @Override
    default int compareTo(T o) {
        return Integer.compare(this.getOrder(), o.getOrder());
    }

}
