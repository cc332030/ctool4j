package com.c332030.ctool4j.mybatis.test.model.impl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c332030.ctool4j.core.util.CPageUtils;
import com.c332030.ctool4j.mybatis.model.impl.CPage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * <p>
 * Description: CPageTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CPageTests {

    @Test
    public void defaultValues() {
        CPage page = new CPage();
        Assertions.assertEquals(1, page.getPageNum());
        Assertions.assertEquals(CPageUtils.DEFAULT_PAGE_SIZE, page.getPageSize());
        Assertions.assertNotNull(page.getOrders());
        Assertions.assertEquals(0, page.getOrders().size());
    }

    @Test
    public void builder() {
        CPage page = CPage.builder()
            .pageNum(2)
            .pageSize(20)
            .orders(Collections.singletonList(OrderItem.asc("id")))
            .build();
        Assertions.assertEquals(2, page.getPageNum());
        Assertions.assertEquals(20, page.getPageSize());
        Assertions.assertEquals(1, page.getOrders().size());
    }

    @Test
    public void getStart() {
        Assertions.assertEquals(0, new CPage(1, 10, null).getStart());
        Assertions.assertEquals(10, new CPage(2, 10, null).getStart());
        Assertions.assertEquals(99, new CPage(10, 11, null).getStart());
    }

    @Test
    public void getStartPageNumOne() {
        // 第一页 start 为 0
        CPage page = CPage.builder().pageNum(1).pageSize(10).build();
        Assertions.assertEquals(0, page.getStart());
    }

    @Test
    public void getLimitSql() {
        Assertions.assertEquals("limit 0 10", new CPage(1, 10, null).getLimitSql());
        Assertions.assertEquals("limit 20 10", new CPage(3, 10, null).getLimitSql());
        Assertions.assertEquals("limit 0 20", new CPage(1, 20, null).getLimitSql());
    }

    @Test
    public void getPage() {
        CPage cPage = CPage.builder().pageNum(2).pageSize(5).build();
        Page<Object> page = cPage.getPage();
        Assertions.assertEquals(2L, page.getCurrent());
        Assertions.assertEquals(5L, page.getSize());
        Assertions.assertNotNull(page.orders());
    }

}
