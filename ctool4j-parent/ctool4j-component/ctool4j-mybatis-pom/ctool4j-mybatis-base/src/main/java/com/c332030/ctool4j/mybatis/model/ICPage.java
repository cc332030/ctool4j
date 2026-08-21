package com.c332030.ctool4j.mybatis.model;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.core.util.CNumUtils;
import com.c332030.ctool4j.core.util.CPageUtils;
import com.c332030.ctool4j.core.validation.CAssert;
import com.c332030.ctool4j.definition.annotation.CJsonLog;
import com.c332030.ctool4j.mybatisplus.util.CMpPageUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.val;

import java.util.List;

/**
 * <p>
 * Description: ICPage
 * </p>
 *
 * @since 2025/2/14
 * @see doc/design/mybatis/ICPage.adoc
 */
@CJsonLog
public interface ICPage {

    /**
     * 当前页
     * @return 当前页
     */
    @ApiModelProperty(value = "当前页", required = true)
    default Integer getPageNum() {
        return 1;
    }

    /**
     * 页大小
     * @return 页大小
     */
    @ApiModelProperty(value = "页大小", required = true)
    default Integer getPageSize() {
        return CPageUtils.DEFAULT_PAGE_SIZE;
    }

    /**
     * 排序
     * @return 排序
     */
    @ApiModelProperty(value = "排序")
    default List<OrderItem> getOrders() {
        return CList.of();
    }

    /**
     * 获取起始行号
     * @return 起始行号
     */
    @JsonIgnore
    default Integer getStart() {
        val pageNum = getPageNum();
        val pageSize = getPageSize();
        CAssert.isTrue(CNumUtils.greaterThanZero(pageNum), "pageNum must be greater than 0");
        CAssert.isTrue(CNumUtils.greaterThanZero(pageSize), "pageSize must be greater than 0");
        return (pageNum - 1) * pageSize;
    }

    /**
     * 获取 limit SQL 片段
     * @return limit SQL 片段
     */
    @JsonIgnore
    default String getLimitSql() {
        return "limit " + getStart() + "," + getPageSize();
    }

    /**
     * 获取 MyBatis-Plus 分页对象
     * @param orders 排序条件
     * @param <E> 实体类型
     * @return 分页对象
     */
    @JsonIgnore
    default <E> Page<E> getPage(List<OrderItem> orders) {
        return CMpPageUtils.getPage(this, orders);
    }

    /**
     * 获取 MyBatis-Plus 分页对象（使用默认排序）
     * @param <E> 实体类型
     * @return 分页对象
     */
    @JsonIgnore
    default <E> Page<E> getPage() {
        return getPage(getOrders());
    }

}
