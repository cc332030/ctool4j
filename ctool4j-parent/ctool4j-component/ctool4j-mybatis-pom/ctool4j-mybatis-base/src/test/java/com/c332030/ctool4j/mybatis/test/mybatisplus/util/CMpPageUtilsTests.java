package com.c332030.ctool4j.mybatis.test.mybatisplus.util;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c332030.ctool4j.core.util.CPageUtils;
import com.c332030.ctool4j.mybatis.model.ICPage;
import com.c332030.ctool4j.mybatis.model.impl.CPage;
import com.c332030.ctool4j.mybatisplus.util.CMpPageUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Description: CMpPageUtilsTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CMpPageUtilsTests {

    @Test
    public void getPage() {
        Page<Object> page = CMpPageUtils.getPage(2, 20);
        Assertions.assertEquals(2L, page.getCurrent());
        Assertions.assertEquals(20L, page.getSize());
    }

    @Test
    public void getPageDefaultSize() {
        Page<Object> page = CMpPageUtils.getPage(1, CPageUtils.DEFAULT_PAGE_SIZE);
        Assertions.assertEquals(1L, page.getCurrent());
        Assertions.assertEquals(10L, page.getSize());
    }

    @Test
    public void getPageForQuery() {
        Page<Object> page = CMpPageUtils.getPageForQuery(3);
        Assertions.assertEquals(3L, page.getCurrent());
        Assertions.assertEquals(10L, page.getSize());
    }

    @Test
    public void getPageForJob() {
        Page<Object> page = CMpPageUtils.getPageForJob(1);
        Assertions.assertEquals(1L, page.getCurrent());
        Assertions.assertEquals(100L, page.getSize());
    }

    @Test
    public void getPageForExport() {
        Page<Object> page = CMpPageUtils.getPageForExport(1);
        Assertions.assertEquals(1L, page.getCurrent());
        Assertions.assertEquals(1000L, page.getSize());
    }

    @Test
    public void getPageByICPage() {
        CPage iCPage = CPage.builder().pageNum(2).pageSize(5).build();
        List<OrderItem> orders = Collections.singletonList(OrderItem.asc("id"));
        Page<Object> page = CMpPageUtils.getPage(iCPage, orders);
        Assertions.assertEquals(2L, page.getCurrent());
        Assertions.assertEquals(5L, page.getSize());
        Assertions.assertEquals(1, page.orders().size());
    }

    @Test
    public void getPageByICPageNullOrders() {
        CPage iCPage = CPage.builder().pageNum(1).pageSize(10).build();
        Page<Object> page = CMpPageUtils.getPage(iCPage, null);
        Assertions.assertEquals(1L, page.getCurrent());
        Assertions.assertEquals(10L, page.getSize());
        Assertions.assertNotNull(page.orders());
        Assertions.assertEquals(0, page.orders().size());
    }

    @Test
    public void emptyPage() {
        CPage iCPage = CPage.builder().pageNum(1).pageSize(10).build();
        Page<Object> page = CMpPageUtils.emptyPage(iCPage);
        Assertions.assertEquals(1L, page.getCurrent());
        Assertions.assertEquals(10L, page.getSize());
        Assertions.assertEquals(0L, page.getTotal());
    }

    @Test
    public void pageInterfaceWithImpl() {
        // ICPage 接口默认方法通过 CPage 实例化验证
        ICPage iCPage = CPage.builder().pageNum(3).pageSize(10).build();
        Assertions.assertEquals(3, iCPage.getPageNum());
        Assertions.assertEquals(10, iCPage.getPageSize());
        Assertions.assertEquals(20, iCPage.getStart());
        Assertions.assertEquals("limit 20,10", iCPage.getLimitSql());
    }

}
