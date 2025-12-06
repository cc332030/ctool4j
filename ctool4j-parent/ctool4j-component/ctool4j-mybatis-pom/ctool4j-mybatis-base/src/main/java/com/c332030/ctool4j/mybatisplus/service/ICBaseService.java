package com.c332030.ctool4j.mybatisplus.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.c332030.ctool4j.core.classes.CBeanUtils;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CIdUtils;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.mybatis.util.CBizIdUtils;
import com.c332030.ctool4j.mybatisplus.mapper.CBaseMapper;
import lombok.val;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * Description: ICBaseService
 * </p>
 *
 * @since 2025/11/27
 */
public interface ICBaseService<ENTITY> extends IService<ENTITY> {

    List<OrderItem> ID_ORDER_ITEMS = CList.of(
        OrderItem.desc("id")
    );

    Class<ENTITY> getEntityClass();

    CBaseMapper<ENTITY> getBaseMapper();

    default String getBizId() {
        return CIdUtils.nextIdWithPrefix(getEntityClass());
    }

    default String getBizId(int length) {
        return CIdUtils.nextIdWithPrefix(getEntityClass(), length);
    }

    default ENTITY getEntity() {
        return CReflectUtils.newInstance(getEntityClass());
    }

    default ENTITY getEntityWithBizId() {

        val entity = getEntity();
        CBizIdUtils.setBizId(entity, getBizId());
        return entity;
    }

    default ENTITY getEntity(Object source) {
        return getEntity(source, null);
    }

    default ENTITY getEntityWithBizId(Object source) {
        val entity = getEntity(source);
        CBizIdUtils.setBizId(entity, getBizId());
        return entity;
    }

    default ENTITY getEntity(Object source, ENTITY old) {
        val entity = getEntity();
        if(null != old) {
            CBeanUtils.copy(old, entity);
        }
        CBeanUtils.copy(source, entity);
        return entity;
    }

    default ENTITY getEntityWithBizId(Object source, ENTITY old) {
        val entity = getEntity(source, old);
        CBizIdUtils.setBizId(entity, getBizId());
        return entity;
    }

    default boolean saveIgnore(ENTITY entity) {

        if(null == entity) {
            return false;
        }
        return SqlHelper.retBool(getBaseMapper().insertIgnore(entity));
    }

    default int batchSaveIgnore(Collection<ENTITY> entities) {
        if(CollUtil.isEmpty(entities)) {
            return 0;
        }
        return entities.stream()
            .map(this::saveIgnore)
            .mapToInt(e -> e ? 1 : 0)
            .sum();
    }

    default Opt<ENTITY> getByIdOpt(Serializable id) {
        if(null == id) {
            return Opt.empty();
        }
        return Opt.ofNullable(getById(id));
    }

    default <O, T> T convertValue(O o, SFunction<O, T> column) {
        if(null == o) {
            return null;
        }
        return column.apply(o);
    }

    default <O, T> Set<T> convertValues(Collection<O> collection, SFunction<O, T> column) {
        return CCollUtils.convertSet(collection, column::apply);
    }

    default ENTITY getByValue(ENTITY entity, SFunction<ENTITY, ?> column){
        if(null == entity) {
            return null;
        }
        return getByValue(column, convertValue(entity, column));
    }
    default ENTITY getByValue(SFunction<ENTITY, ?> column, Object value){
        if(null == value) {
            return null;
        }
        return lambdaQuery()
                .eq(column, value)
                .one();
    }

    default List<ENTITY> listByValue(ENTITY entity, SFunction<ENTITY, ?> column){
        if(null == entity) {
            return CList.of();
        }
        return listByValue(column, convertValue(entity, column));
    }

    default List<ENTITY> listByValue(SFunction<ENTITY, ?> column, Object value){
        if(null == value) {
            return CList.of();
        }
        return lambdaQuery()
                .eq(column, value)
                .list();
    }

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

    default boolean removeByValue(ENTITY entity, SFunction<ENTITY, ?> column){
        if(null == entity) {
            return false;
        }
        return removeByValue(column, convertValue(entity, column));
    }

    default boolean removeByValue(SFunction<ENTITY, ?> column, Object value){
        if(null == value) {
            return false;
        }
        return lambdaUpdate()
                .eq(column, value)
                .remove();
    }

    default List<ENTITY> listByValues(Collection<ENTITY> collection, SFunction<ENTITY, ?> column){

        if(CollUtil.isEmpty(collection)) {
            return CList.of();
        }

        val values = convertValues(collection, column);
        return listByValues(column, values);
    }

    default List<ENTITY> listByValues(SFunction<ENTITY, ?> column, Collection<?> values){

        if(CollUtil.isEmpty(values)) {
            return CList.of();
        }

        return lambdaQuery()
                .in(column, values)
                .list();
    }

    default boolean removeByValues(Collection<ENTITY> collection, SFunction<ENTITY, ?> column){

        if(CollUtil.isEmpty(collection)) {
            return false;
        }

        val values = convertValues(collection, column);
        return removeByValues(column, values);
    }

    default boolean removeByValues(SFunction<ENTITY, ?> column, Collection<?> values){

        if(CollUtil.isEmpty(values)) {
            return false;
        }

        return lambdaUpdate()
                .in(column, values)
                .remove();
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
