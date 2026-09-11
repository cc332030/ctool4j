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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证 CPage 的默认值、builder 构建、起始位置计算、limit SQL 生成与分页对象获取。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 pageNum/pageSize/orders 默认值及 getStart/getLimitSql/getPage 的约定。</li>
 *   <li>依据测试方法（等价类/边界）：默认值、builder、起始位置、limit SQL、分页对象。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：默认值、builder 构建、getStart 起始位置、getLimitSql 生成、getPage 分页对象。</li>
 *   <li>未覆盖：真实 MyBatis 分页拦截器集成场景。</li>
 * </ul>
 * <h2>分页模型行为</h2>
 * <ul>
 *   <li>1.1 默认值（{@code defaultValues}）</li>
 *   <li>1.2 builder 构建（{@code builder}）</li>
 *   <li>1.3 起始位置 getStart（{@code getStart}）</li>
 *   <li>1.4 pageNum 为 1 的起始位置（{@code getStartPageNumOne}）</li>
 *   <li>1.5 limit SQL 生成（{@code getLimitSql}）</li>
 *   <li>1.6 分页对象获取（{@code getPage}）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CPageTests {

        /**
         * 对应测试用例 1.1：默认值（{@code defaultValues}）
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
         * 对应测试用例 1.2：builder 构建（{@code builder}）
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
         * 对应测试用例 1.3：起始位置 getStart（{@code getStart}）
         */
    @Test
    public void getStart() {
        Assertions.assertEquals(0, CPage.builder().pageNum(1).pageSize(10).build().getStart());
        Assertions.assertEquals(10, CPage.builder().pageNum(2).pageSize(10).build().getStart());
        Assertions.assertEquals(99, CPage.builder().pageNum(10).pageSize(11).build().getStart());
    }

        /**
         * 对应测试用例 1.4：pageNum 为 1 的起始位置（{@code getStartPageNumOne}）
         */
    @Test
    public void getStartPageNumOne() {
        // 第一页 start 为 0
        CPage page = CPage.builder().pageNum(1).pageSize(10).build();
        Assertions.assertEquals(0, page.getStart());
    }

        /**
         * 对应测试用例 1.5：limit SQL 生成（{@code getLimitSql}）
         */
    @Test
    public void getLimitSql() {
        // MySQL 方言 LIMIT offset, count 格式
        Assertions.assertEquals("limit 0,10", CPage.builder().pageNum(1).pageSize(10).build().getLimitSql());
        Assertions.assertEquals("limit 20,10", CPage.builder().pageNum(3).pageSize(10).build().getLimitSql());
        Assertions.assertEquals("limit 0,20", CPage.builder().pageNum(1).pageSize(20).build().getLimitSql());
    }

        /**
         * 对应测试用例 1.6：分页对象获取（{@code getPage}）
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
