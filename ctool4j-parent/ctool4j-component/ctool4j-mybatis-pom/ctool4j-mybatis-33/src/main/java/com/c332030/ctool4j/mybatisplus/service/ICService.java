package com.c332030.ctool4j.mybatisplus.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.c332030.ctool4j.core.util.CCollUtils;
import lombok.val;

import java.util.Collection;

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
            return 0;
        }
        return countByValue(column, column.apply(entity));
    }
    default Integer countByValue(SFunction<T, ?> column, Object value){
        if(null == value) {
            return 0;
        }
        return lambdaQuery()
            .eq(column, value)
            .count();
    }

    default Integer countByValues(Collection<T> collection, SFunction<T, ?> column){

        if(CollUtil.isEmpty(collection)) {
            return 0;
        }

        val values = CCollUtils.convertSet(collection, column::apply);
        return countByValues(column, values);
    }

    default Integer countByValues(SFunction<T, ?> column, Collection<?> values){

        if(CollUtil.isEmpty(values)) {
            return 0;
        }
        return lambdaQuery()
            .in(column, values)
            .count();
    }

}
