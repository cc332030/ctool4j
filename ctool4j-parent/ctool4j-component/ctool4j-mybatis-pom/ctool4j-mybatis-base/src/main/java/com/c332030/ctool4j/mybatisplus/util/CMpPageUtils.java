package com.c332030.ctool4j.mybatisplus.util;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CPageUtils;
import com.c332030.ctool4j.core.validation.CValidUtils;
import com.c332030.ctool4j.definition.function.CConsumer;
import com.c332030.ctool4j.definition.function.CFunction;
import com.c332030.ctool4j.mybatis.model.ICPage;
import com.c332030.ctool4j.mybatis.model.impl.CPageResult;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.List;

/**
 * <p>
 * Description: CMpPageUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CMpPageUtils}：分页工具。</p>
 * <ul>
 *   <li>分页对象创建：查询/任务/导出场景的单次大小、由 {@code ICPage} 创建、空分页</li>
 *   <li>分页对象转换：MP 分页对象转 {@code CPageResult}（不依赖 MP 的响应契约）</li>
 * </ul>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>创建不同单次大小的 Page（查询10/任务100/导出1000）</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>默认单次大小；{@code getPageResult} 入参为 null 时返回默认分页结果（空数据、总数为 0）</p>
 * <h2>适用范围</h2>
 * <p>分页创建、MP 分页对象到响应契约的转换</p>
 * <h2>不适用与边界场景</h2>
 * <p>静态工具</p>
 * <h2>已知限制与取舍</h2>
 * <p>静态工具</p>
 *
 * @since 2025/12/9
 * @version 1.1
 */
@UtilityClass
public class CMpPageUtils {

    /**
     * 查询创建 Page，单次 10
     * @param pageNum 当前页
     * @return Page
     * @param <T> 泛型
     */
    public <T> Page<T> getPageForQuery(Integer pageNum) {
        return getPage(pageNum, CPageUtils.DEFAULT_PAGE_SIZE);
    }

    /**
     * 定时任务创建 Page，单次 100
     * @param pageNum 当前页
     * @return Page
     * @param <T> 泛型
     */
    public <T> Page<T> getPageForJob(Integer pageNum) {
        return getPage(pageNum, CPageUtils.DEFAULT_JOB_PAGE_SIZE);
    }

    /**
     * 导出创建 Page，单次 1000
     * @param pageNum 当前页
     * @return Page
     * @param <T> 泛型
     */
    public <T> Page<T> getPageForExport(Integer pageNum) {
        return getPage(pageNum, CPageUtils.DEFAULT_EXPORT_PAGE_SIZE);
    }

    /**
     * 创建 Page
     * @param pageNum 当前页
     * @param pageSize 页大小
     * @return Page
     * @param <T> 泛型
     */
    public <T> Page<T> getPage(Integer pageNum, Integer pageSize) {
        return new Page<>(pageNum, pageSize);
    }

    /**
     * 通过 ICPage 创建 Page
     * @param iCPage iCPage
     * @param orders 排序
     * @return Page
     * @param <T> 泛型
     */
    public <T> Page<T> getPage(ICPage iCPage, List<OrderItem> orders) {

        val page = new Page<T>(iCPage.getPageNum(), iCPage.getPageSize());
        page.setOrders(CCollUtils.defaultEmpty(orders));
        return page;
    }

    /**
     * 创建一个空的Page
     * @param iCPage iCPage
     * @return Page
     * @param <T> 泛型
     */
    public <T> Page<T> emptyPage(ICPage iCPage) {
        return new Page<>(iCPage.getPageNum(), iCPage.getPageSize(), 0);
    }

    /**
     * 由 MP 分页对象创建分页结果
     *
     * <p>响应契约 {@code CPageResult} 不引用 MyBatis-Plus 类型，供无法依赖 MP 的引用方直接使用。</p>
     *
     * <p>page 为 null 时返回默认分页结果（current=1、size=0、total=0、pages=0、records 为空列表），不抛异常。</p>
     *
     * @param page MP 分页对象
     * @param <T> 数据类型
     * @return 分页结果
     */
    public <T> CPageResult<T> getPageResult(IPage<T> page) {

        if(CValidUtils.isNotValid(page)) {
            return CPageResult.<T>builder().build();
        }

        return CPageResult.<T>builder()
            .current(page.getCurrent())
            .size(page.getSize())
            .total(page.getTotal())
            .pages(page.getPages())
            .records(page.getRecords())
            .build();
    }

    /**
     * 分页查询并执行逻辑
     * @param queryFunction 分页查询
     * @param doSth 执行逻辑
     * @param <T> 数据类型
     */
    public <T> void pageThenDo(
        CFunction<Integer, IPage<T>> queryFunction,
        CConsumer<List<T>> doSth
    ) {
        CPageUtils.pageThenDo(
            queryFunction,
            page -> {

                val records = page.getRecords();
                if(CollUtil.isEmpty(records)) {
                    return false;
                }

                doSth.accept(records);
                return true;
            }
        );
    }

    /**
     * 分页查询并执行逻辑
     * @param queryFunction 分页查询
     * @param doSth 执行逻辑
     * @param <T> 数据类型
     */
    public <T> void pageThenEach(
        CFunction<Integer, IPage<T>> queryFunction,
        CConsumer<T> doSth
    ) {
        pageThenDo(
            queryFunction,
            list -> list.forEach(doSth)
        );
    }

}
