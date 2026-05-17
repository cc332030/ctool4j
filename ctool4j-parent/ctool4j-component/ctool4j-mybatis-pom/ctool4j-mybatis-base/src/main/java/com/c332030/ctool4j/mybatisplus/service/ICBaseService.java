package com.c332030.ctool4j.mybatisplus.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.c332030.ctool4j.core.classes.CBeanUtils;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CEntityUtils;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.core.util.CSet;
import com.c332030.ctool4j.core.validation.CValidateUtils;
import com.c332030.ctool4j.definition.function.CFunction;
import com.c332030.ctool4j.mybatis.model.impl.CPageReq;
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
public interface ICBaseService<ENTITY> extends ICBizIdService<ENTITY> {

    List<OrderItem> ID_ORDER_ITEMS = CList.of(
        OrderItem.desc("id")
    );

    default String getEntitySimpleName() {
        return getEntityClass().getSimpleName();
    }

    CBaseMapper<ENTITY> getBaseMapper();

    default ENTITY getEntity() {
        return CReflectUtils.newInstance(getEntityClass());
    }

    default ENTITY getEntity(Object... sources) {
        val entity = getEntity();
        CBeanUtils.copyFromArr(sources, entity);
        CEntityUtils.clear(entity);
        return entity;
    }

    default ENTITY getEntityWithBizId() {
        val entity = getEntity();
        CBizIdUtils.setBizId(entity, this);
        return entity;
    }

    default ENTITY getEntityWithBizId(Object... sources) {
        val entity = getEntity(sources);
        CBizIdUtils.setBizId(entity, this);
        return entity;
    }

    default IPage<ENTITY> page(CPageReq<?> pageReq) {

        val reqMap = CBeanUtils.toMapUnderlineName(pageReq.getReq());
        if(CValidateUtils.isEmpty(reqMap)) {
            return page(pageReq.getPage());
        }

        val queryWrapper = Wrappers.<ENTITY>query()
            .allEq(reqMap);

        return page(pageReq.getPage(), queryWrapper);
    }

    default <RET> IPage<RET> page(
        CPageReq<?> pageReq,
        CFunction<ENTITY, RET> function
    ) {
        val page = page(pageReq);
        return page.convert(function);
    }

    default <RET> IPage<RET> page(
        CPageReq<?> pageReq,
        Class<RET> retClass
    ) {
        return page(pageReq, e -> CBeanUtils.copy(e, retClass));
    }

    default <RET> IPage<RET> pageConvert(
        CPageReq<?> pageReq,
        CFunction<IPage<ENTITY>, IPage<RET>> function
    ) {
        val page = page(pageReq);
        if(CollUtil.isEmpty(page.getRecords())) {
            return page.convert(CFunction.empty());
        }
        return function.apply(page);
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

    default boolean updateAllById(ENTITY entity) {
        if(null == entity) {
            return false;
        }
        return SqlHelper.retBool(getBaseMapper().updateAllById(entity));
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
        if(CollUtil.isEmpty(collection)) {
            return CSet.of();
        }
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

    default boolean updateByValue(ENTITY entity, SFunction<ENTITY, ?> column){
        if(null == entity) {
            return false;
        }
        val value = convertValue(entity, column);
        if(null == value) {
            return false;
        }
        return lambdaUpdate()
                .eq(column, value)
                .update(entity);
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

}
