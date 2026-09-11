package com.c332030.ctool4j.spring.interfaces;

import org.springframework.core.Ordered;

/**
 * <p>
 * Description: ICOrdered
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICOrdered}：排序接口。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>提供排序能力（继承 Ordered）</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>默认序</p>
 * <h2>适用范围</h2>
 * <p>排序控制</p>
 * <h2>不适用与边界场景</h2>
 * <p>实现 Ordered</p>
 * <h2>已知限制与取舍</h2>
 * <p>实现 Ordered</p>
 *
 * @since 2025/9/28
 * @version 1.0
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
