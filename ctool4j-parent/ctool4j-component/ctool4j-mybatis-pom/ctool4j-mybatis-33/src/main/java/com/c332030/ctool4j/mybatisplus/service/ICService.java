package com.c332030.ctool4j.mybatisplus.service;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

/**
 * <p>
 * Description: ICBaseService
 * </p>
 *
 * @since 2025/12/4
 */
public interface ICService<ENTITY> extends ICBaseService<ENTITY> {

    default Integer countByValue(ENTITY entity, SFunction<ENTITY, ?> column){
        if(null == entity) {
            return 0;
        }
        return countByValue(column, convertValue(entity, column));
    }
    default Integer countByValue(SFunction<ENTITY, ?> column, Object value){
        if(null == value) {
            return 0;
        }
        return lambdaQuery()
                .eq(column, value)
                .count();
    }

}
