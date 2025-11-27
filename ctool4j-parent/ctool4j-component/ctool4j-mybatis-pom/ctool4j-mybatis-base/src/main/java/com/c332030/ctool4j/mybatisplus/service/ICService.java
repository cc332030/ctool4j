package com.c332030.ctool4j.mybatisplus.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.c332030.ctool4j.core.util.CIdUtils;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.mybatisplus.mapper.CBaseMapper;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * Description: ICService
 * </p>
 *
 * @since 2025/11/27
 */
public interface ICService<T> extends IService<T> {

    Class<T> getEntityClass();

    CBaseMapper<T> getBaseMapper();

    default String getBizId() {
        return CIdUtils.nextIdWithPrefix(getEntityClass());
    }

    default String getBizId(int length) {
        return CIdUtils.nextIdWithPrefix(getEntityClass(), length);
    }

    default boolean saveIgnore(T entity) {
        return SqlHelper.retBool(getBaseMapper().insertIgnore(entity));
    }

    default T getByValue(SFunction<T, ?> column, Object value){
        return lambdaQuery()
                .eq(column, value)
                .one();
    }

    default List<T> listByValue(SFunction<T, ?> column, Object value){
        return lambdaQuery()
                .eq(column, value)
                .list();
    }

    default List<T> listByValues(SFunction<T, ?> column, Collection<?> values){

        if(CollUtil.isEmpty(values)) {
            return CList.of();
        }

        return lambdaQuery()
                .in(column, values)
                .list();
    }

}
