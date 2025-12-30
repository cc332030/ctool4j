package com.c332030.ctool4j.mybatisplus.util;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CPageUtils;
import com.c332030.ctool4j.definition.function.CConsumer;
import com.c332030.ctool4j.definition.function.CFunction;
import com.c332030.ctool4j.mybatis.model.ICPage;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.List;

/**
 * <p>
 * Description: CMpPageUtils
 * </p>
 *
 * @since 2025/12/9
 */
@UtilityClass
public class CMpPageUtils {

    /**
     * 创建 Page
     * @param pageNum 当前页
     * @return Page
     * @param <T> 泛型
     */
    public <T> Page<T> getPageForQuery(Integer pageNum) {
        return getPage(pageNum, CPageUtils.DEFAULT_LIST_PAGE_SIZE);
    }

    /**
     * 创建 Page
     * @param pageNum 当前页
     * @return Page
     * @param <T> 泛型
     */
    public <T> Page<T> getPageForTask(Integer pageNum) {
        return getPage(pageNum, CPageUtils.DEFAULT_JOB_PAGE_SIZE);
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
