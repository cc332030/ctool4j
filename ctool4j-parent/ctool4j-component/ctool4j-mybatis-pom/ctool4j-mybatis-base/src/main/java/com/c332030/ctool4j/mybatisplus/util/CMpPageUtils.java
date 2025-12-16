package com.c332030.ctool4j.mybatisplus.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c332030.ctool4j.mybatis.model.ICPage;
import lombok.experimental.UtilityClass;

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
     * 创建一个空的Page
     * @param iCPage iCPage
     * @return Page
     * @param <T> 泛型
     */
    public <T> Page<T> emptyPage(ICPage iCPage) {
        return new Page<T>(iCPage.getPageNum(), iCPage.getPageSize(), 0);
    }

}
