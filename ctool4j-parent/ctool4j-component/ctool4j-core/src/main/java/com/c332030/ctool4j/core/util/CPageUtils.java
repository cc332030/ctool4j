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

    /**
     * 默认分页大小
     */
    public static final Integer DEFAULT_PAGE_SIZE = 10;

    /**
     * 定时任务分页大小
     */
    public static final Integer DEFAULT_JOB_PAGE_SIZE = 100;

    /**
     * 导出分页大小
     */
    public static final Integer DEFAULT_EXPORT_PAGE_SIZE = 1000;

    /**
     * 分页查询并执行逻辑
     *
     * <p>终止条件：queryFunction 返回 null、返回集合为空，或 doSth 返回 false</p>
     *
     * <p>当返回自定义分页对象（非集合，如 IPage）时，无法通过 CollUtil.isEmpty 判断是否结束，
     * 需由业务在无数据时返回 null，或让 doSth 在结束场景返回 false</p>
     *
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

}
