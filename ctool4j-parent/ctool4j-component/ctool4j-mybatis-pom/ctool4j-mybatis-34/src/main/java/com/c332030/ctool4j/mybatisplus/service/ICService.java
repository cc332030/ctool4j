package com.c332030.ctool4j.mybatisplus.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.val;

import java.util.Collection;

/**
 * <p>
 * Description: ICBaseService
 * </p>
 *
 * @since 2025/12/4
 */
public interface ICService<ENTITY> extends ICBaseService<ENTITY> {

    default Long countByValue(ENTITY entity, SFunction<ENTITY, ?> column){
        if(null == entity) {
            return 0L;
        }
        return countByValue(column, convertValue(entity, column));
    }
    default Long countByValue(SFunction<ENTITY, ?> column, Object value){
        if(null == value) {
            return 0L;
        }
        return lambdaQuery()
                .eq(column, value)
                .count();
    }

    default Long countByValues(Collection<ENTITY> collection, SFunction<ENTITY, ?> column){

        if(CollUtil.isEmpty(collection)) {
            return 0L;
        }

        val values = convertValues(collection, column);
        return countByValues(column, values);
    }

    default Long countByValues(SFunction<ENTITY, ?> column, Collection<?> values){

        if(CollUtil.isEmpty(values)) {
            return 0L;
        }
        return lambdaQuery()
                .in(column, values)
                .count();
    }

}
