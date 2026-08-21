package com.c332030.ctool4j.mybatis.test.model.impl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c332030.ctool4j.core.util.CPageUtils;
import com.c332030.ctool4j.mybatis.model.impl.CPage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

/**
 * <p>
 * Description: CPageTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CPageTests {

        /**
     * 对应测试用例 1.1
     */
    @Test
    public void defaultValues() {
        CPage page = new CPage();
        Assertions.assertEquals(1, page.getPageNum());
        Assertions.assertEquals(CPageUtils.DEFAULT_PAGE_SIZE, page.getPageSize());
        Assertions.assertNotNull(page.getOrders());
        Assertions.assertEquals(0, page.getOrders().size());
    }

        /**
     * 对应测试用例 1.2
     */
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

        /**
     * 对应测试用例 1.3
     */
    @Test
    public void getStart() {
        Assertions.assertEquals(0, CPage.builder().pageNum(1).pageSize(10).build().getStart());
        Assertions.assertEquals(10, CPage.builder().pageNum(2).pageSize(10).build().getStart());
        Assertions.assertEquals(99, CPage.builder().pageNum(10).pageSize(11).build().getStart());
    }

        /**
     * 对应测试用例 1.4
     */
    @Test
    public void getStartPageNumOne() {
        // 第一页 start 为 0
        CPage page = CPage.builder().pageNum(1).pageSize(10).build();
        Assertions.assertEquals(0, page.getStart());
    }

        /**
     * 对应测试用例 1.5
     */
    @Test
    public void getLimitSql() {
        // MySQL 方言 LIMIT offset, count 格式
        Assertions.assertEquals("limit 0,10", CPage.builder().pageNum(1).pageSize(10).build().getLimitSql());
        Assertions.assertEquals("limit 20,10", CPage.builder().pageNum(3).pageSize(10).build().getLimitSql());
        Assertions.assertEquals("limit 0,20", CPage.builder().pageNum(1).pageSize(20).build().getLimitSql());
    }

        /**
     * 对应测试用例 1.6
     */
    @Test
    public void getPage() {
        CPage cPage = CPage.builder().pageNum(2).pageSize(5).build();
        Page<Object> page = cPage.getPage();
        Assertions.assertEquals(2L, page.getCurrent());
        Assertions.assertEquals(5L, page.getSize());
        Assertions.assertNotNull(page.orders());
    }

}
