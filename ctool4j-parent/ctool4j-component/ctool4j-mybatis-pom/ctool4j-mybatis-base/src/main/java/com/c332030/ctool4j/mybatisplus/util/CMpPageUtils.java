package com.c332030.ctool4j.mybatisplus.util;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c332030.ctool4j.core.util.CCollUtils;
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

}
