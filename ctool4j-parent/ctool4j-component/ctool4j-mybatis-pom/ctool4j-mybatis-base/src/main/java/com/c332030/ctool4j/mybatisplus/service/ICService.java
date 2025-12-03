package com.c332030.ctool4j.mybatisplus.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.c332030.ctool4j.core.classes.CBeanUtils;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.util.CIdUtils;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.mybatis.util.CBizIdUtils;
import com.c332030.ctool4j.mybatisplus.mapper.CBaseMapper;
import lombok.val;

import java.io.Serializable;
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

    List<OrderItem> ID_ORDER_ITEMS = CList.of(
        OrderItem.desc("id")
    );

    Class<T> getEntityClass();

    CBaseMapper<T> getBaseMapper();

    default String getBizId() {
        return CIdUtils.nextIdWithPrefix(getEntityClass());
    }

    default String getBizId(int length) {
        return CIdUtils.nextIdWithPrefix(getEntityClass(), length);
    }

    default T getEntity() {
        return CReflectUtils.newInstance(getEntityClass());
    }

    default T getEntityWithBizId() {

        val entity = getEntity();
        CBizIdUtils.setBizId(entity, getBizId());
        return entity;
    }

    default T getEntity(Object source) {
        return getEntity(source, null);
    }

    default T getEntityWithBizId(Object source) {
        val entity = getEntity(source);
        CBizIdUtils.setBizId(entity, getBizId());
        return entity;
    }

    default T getEntity(Object source, T old) {
        val entity = getEntity();
        if(null != old) {
            CBeanUtils.copy(old, entity);
        }
        CBeanUtils.copy(source, entity);
        return entity;
    }

    default T getEntityWithBizId(Object source, T old) {
        val entity = getEntity(source, old);
        CBizIdUtils.setBizId(entity, getBizId());
        return entity;
    }

    default boolean saveIgnore(T entity) {
        return SqlHelper.retBool(getBaseMapper().insertIgnore(entity));
    }

    default Opt<T> getByIdOpt(Serializable id) {
        return Opt.ofNullable(getById(id));
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
