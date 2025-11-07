package com.c332030.ctool4j.core.util;

import cn.hutool.core.collection.CollUtil;
import com.c332030.ctool4j.core.function.CConsumer;
import com.c332030.ctool4j.core.function.CFunction;
import lombok.SneakyThrows;
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
    public <T> void queryThenDo(
            CFunction<Integer, List<T>> queryFunction,
            CConsumer<T> doSth
    ) {

        var start = 1;
        while (true) {

            val list = queryFunction.apply(start);
            if(CollUtil.isEmpty(list)) {
                break;
            }

            for (T t : list) {
                doSth.accept(t);
            }

            start++;

        }

    }

}
