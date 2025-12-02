package com.c332030.ctool4j.mybatis.model;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.definition.annotation.CJsonLog;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.val;

import java.util.List;

/**
 * <p>
 * Description: ICPage
 * </p>
 *
 * @since 2025/2/14
 */
@CJsonLog
public interface ICPage {

    /**
     * 当前页
     * @return 当前页
     */
    Integer getPageNum();

    /**
     * 页大小
     * @return 页大小
     */
    Integer getPageSize();

    /**
     * 排序
     * @return 排序
     */
    default List<OrderItem> getOrders() {
        return CList.of();
    }

    @JsonIgnore
    default Integer getStart() {
        return (getPageNum() - 1) * getPageSize();
    }

    @JsonIgnore
    default String getLimitSql() {
        return "limit " + getStart() + " " + getPageSize();
    }

    @JsonIgnore
    default <E> Page<E> getPage(List<OrderItem> orders) {
        val page = new Page<E>(getPageNum(), getPageSize());
        page.setOrders(orders);
        return page;
    }

    @JsonIgnore
    default <E> Page<E> getPage() {
        return getPage(getOrders());
    }

}
