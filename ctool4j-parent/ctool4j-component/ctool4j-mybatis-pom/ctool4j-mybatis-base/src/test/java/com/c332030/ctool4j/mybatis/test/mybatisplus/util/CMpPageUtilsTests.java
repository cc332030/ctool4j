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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证创建 Page 的各路径：默认大小、查询/任务/导出的单次大小、由 ICPage 创建、null orders 边界。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对不同场景单次分页大小的约定。</li>
 *   <li>依据测试方法（等价类/边界）：默认大小、各场景大小、ICPage 转换、null orders。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：getPage 默认大小；getPageForQuery/Job/Export 各大小；由 ICPage 创建；null orders 边界。</li>
 *   <li>未覆盖：真实 MyBatis-Plus 分页拦截器集成。</li>
 * </ul>
 * <h2>分页创建</h2>
 * <ul>
 *   <li>1.1 getPage 默认大小（{@code getPageDefaultSize}）</li>
 *   <li>1.2 getPage（{@code getPage}）</li>
 *   <li>1.3 查询分页（{@code getPageForQuery}）</li>
 *   <li>1.4 任务分页（{@code getPageForJob}）</li>
 *   <li>1.5 导出版分页（{@code getPageForExport}）</li>
 *   <li>1.6 由 ICPage 创建（{@code getPageByICPage}）</li>
 *   <li>1.7 ICPage null orders 边界（{@code getPageByICPageNullOrders}）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CMpPageUtilsTests {

        /**
         * 对应测试用例 1.1：getPage 默认大小（{@code getPageDefaultSize}）
         */
    @Test
    public void getPage() {
        Page<Object> page = CMpPageUtils.getPage(2, 20);
        Assertions.assertEquals(2L, page.getCurrent());
        Assertions.assertEquals(20L, page.getSize());
    }

        /**
         * 对应测试用例 1.2：getPage（{@code getPage}）
         */
    @Test
    public void getPageDefaultSize() {
        Page<Object> page = CMpPageUtils.getPage(1, CPageUtils.DEFAULT_PAGE_SIZE);
        Assertions.assertEquals(1L, page.getCurrent());
        Assertions.assertEquals(10L, page.getSize());
    }

        /**
         * 对应测试用例 1.3：查询分页（{@code getPageForQuery}）
         */
    @Test
    public void getPageForQuery() {
        Page<Object> page = CMpPageUtils.getPageForQuery(3);
        Assertions.assertEquals(3L, page.getCurrent());
        Assertions.assertEquals(10L, page.getSize());
    }

        /**
         * 对应测试用例 1.4：任务分页（{@code getPageForJob}）
         */
    @Test
    public void getPageForJob() {
        Page<Object> page = CMpPageUtils.getPageForJob(1);
        Assertions.assertEquals(1L, page.getCurrent());
        Assertions.assertEquals(100L, page.getSize());
    }

        /**
         * 对应测试用例 1.5：导出版分页（{@code getPageForExport}）
         */
    @Test
    public void getPageForExport() {
        Page<Object> page = CMpPageUtils.getPageForExport(1);
        Assertions.assertEquals(1L, page.getCurrent());
        Assertions.assertEquals(1000L, page.getSize());
    }

        /**
         * 对应测试用例 1.6：由 ICPage 创建（{@code getPageByICPage}）
         */
    @Test
    public void getPageByICPage() {
        CPage iCPage = CPage.builder().pageNum(2).pageSize(5).build();
        List<OrderItem> orders = Collections.singletonList(OrderItem.asc("id"));
        Page<Object> page = CMpPageUtils.getPage(iCPage, orders);
        Assertions.assertEquals(2L, page.getCurrent());
        Assertions.assertEquals(5L, page.getSize());
        Assertions.assertEquals(1, page.orders().size());
    }

        /**
         * 对应测试用例 1.7：ICPage null orders 边界（{@code getPageByICPageNullOrders}）
         */
    @Test
    public void getPageByICPageNullOrders() {
        CPage iCPage = CPage.builder().pageNum(1).pageSize(10).build();
        Page<Object> page = CMpPageUtils.getPage(iCPage, null);
        Assertions.assertEquals(1L, page.getCurrent());
        Assertions.assertEquals(10L, page.getSize());
        Assertions.assertNotNull(page.orders());
        Assertions.assertEquals(0, page.orders().size());
    }

        /**
         * 对应测试用例 1.8
         */
    @Test
    public void emptyPage() {
        CPage iCPage = CPage.builder().pageNum(1).pageSize(10).build();
        Page<Object> page = CMpPageUtils.emptyPage(iCPage);
        Assertions.assertEquals(1L, page.getCurrent());
        Assertions.assertEquals(10L, page.getSize());
        Assertions.assertEquals(0L, page.getTotal());
    }

        /**
         * 对应测试用例 1.9
         */
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
