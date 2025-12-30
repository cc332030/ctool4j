package com.c332030.ctool4j.core.util;

import cn.hutool.core.collection.CollUtil;
import com.c332030.ctool4j.definition.function.CConsumer;
import com.c332030.ctool4j.definition.function.CFunction;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;

import java.util.List;

/**
 * <p>
 * Description: CPageUtils
 * </p>
 *
 * @since 2025/10/31
 */
@UtilityClass
public class CPageUtils {

    public static final Integer DEFAULT_LIST_PAGE_SIZE = 10;

    public static final Integer DEFAULT_JOB_PAGE_SIZE = 1000;

    /**
     * 分页查询并执行逻辑
     * @param queryFunction 分页查询
     * @param doSth 执行逻辑
     * @param <T> 数据类型
     */
    public <T> void pageThenDo(
            CFunction<Integer, T> queryFunction,
            CFunction<T, Boolean> doSth
    ) {

        var start = 1;
        while (true) {

            val result = queryFunction.apply(start);
            if(null == result
                || CBoolUtils.isNotTrue(doSth.apply(result))
            ) {
                break;
            }
            start++;
        }

    }

    /**
     * 分页查询并执行逻辑
     * @param queryFunction 分页查询
     * @param doSth 执行逻辑
     * @param <T> 数据类型
     */
    public <T> void pageThenEach(
        CFunction<Integer, List<T>> queryFunction,
        CConsumer<T> doSth
    ) {
        pageThenDo(
            queryFunction,
            list -> {
                if(CollUtil.isEmpty(list)) {
                    return false;
                }
                list.forEach(doSth);
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
    @Deprecated
    public <T> void queryThenDo(
            CFunction<Integer, List<T>> queryFunction,
            CConsumer<T> doSth
    ) {
        pageThenEach(queryFunction, doSth);
    }

}
