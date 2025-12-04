package com.c332030.ctool4j.mybatisplus.service;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

/**
 * <p>
 * Description: ICBaseService
 * </p>
 *
 * @since 2025/12/4
 */
public interface ICService<T> extends ICBaseService<T> {

    default Integer countByValue(T entity, SFunction<T, ?> column){
        if(null == entity) {
            return null;
        }
        return countByValue(column, column.apply(entity));
    }
    default Integer countByValue(SFunction<T, ?> column, Object value){
        if(null == value) {
            return null;
        }
        return lambdaQuery()
            .eq(column, value)
            .count();
    }

}
