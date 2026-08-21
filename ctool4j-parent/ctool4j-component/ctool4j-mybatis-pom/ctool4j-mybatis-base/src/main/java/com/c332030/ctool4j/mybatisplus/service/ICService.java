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
 * Description: ICService
 * </p>
 *
 * @since 2025/11/27
 * @see doc/design/mybatisplus/ICService.adoc
 */
public interface ICService<ENTITY> extends ICBizIdService<ENTITY> {

    /**
     * ID 倒序排序列
     */
    List<OrderItem> ID_ORDER_ITEMS = CList.of(
        OrderItem.desc("id")
    );

    /**
     * 获取实体类简单名称
     *
     * @return 实体类简单名称
     */
    default String getEntitySimpleName() {
        return getEntityClass().getSimpleName();
    }

    /**
     * 获取基础 Mapper
     *
     * @return 基础 Mapper
     */
    CBaseMapper<ENTITY> getBaseMapper();

    /**
     * 创建空实体对象
     *
     * @return 空实体对象
     */
    default ENTITY getEntity() {
        return CReflectUtils.newInstance(getEntityClass());
    }

    /**
     * 从多个数据源创建实体对象
     *
     * @param sources 数据源对象
     * @return 创建后的实体对象
     */
    default ENTITY getEntity(Object... sources) {
        val entity = getEntity();
        CBeanUtils.copyFromArr(sources, entity);
        CEntityUtils.clear(entity);
        return entity;
    }

    /**
     * 创建带业务 ID 的空实体对象
     *
     * @return 带业务 ID 的实体对象
     */
    default ENTITY getEntityWithBizId() {
        val entity = getEntity();
        CBizIdUtils.setBizId(entity, this);
        return entity;
    }

    /**
     * 从多个数据源创建带业务 ID 的实体对象
     *
     * @param sources 数据源对象
     * @return 带业务 ID 的实体对象
     */
    default ENTITY getEntityWithBizId(Object... sources) {
        val entity = getEntity(sources);
        CBizIdUtils.setBizId(entity, this);
        return entity;
    }

    /**
     * 分页查询
     *
     * @param pageReq 分页请求
     * @return 分页结果
     */
    default IPage<ENTITY> page(CPageReq<?> pageReq) {

        val reqMap = CBeanUtils.toMapUnderlineName(pageReq.getReq());
        if(CValidateUtils.isEmpty(reqMap)) {
            return page(pageReq.getPage());
        }

        val queryWrapper = Wrappers.<ENTITY>query()
            .allEq(reqMap);

        return page(pageReq.getPage(), queryWrapper);
    }

    /**
     * 分页查询并转换结果
     *
     * @param pageReq 分页请求
     * @param function 转换函数
     * @param <RET> 转换结果类型
     * @return 转换后的分页结果
     */
    default <RET> IPage<RET> page(
        CPageReq<?> pageReq,
        CFunction<ENTITY, RET> function
    ) {
        val page = page(pageReq);
        return page.convert(function);
    }

    /**
     * 分页查询并转换为指定类型
     *
     * @param pageReq 分页请求
     * @param retClass 转换结果类型
     * @param <RET> 转换结果类型
     * @return 转换后的分页结果
     */
    default <RET> IPage<RET> page(
        CPageReq<?> pageReq,
        Class<RET> retClass
    ) {
        return page(pageReq, e -> CBeanUtils.copy(e, retClass));
    }

    /**
     * 分页查询后按函数转换
     *
     * @param pageReq 分页请求
     * @param function 转换函数
     * @param <RET> 转换结果类型
     * @return 转换后的分页结果
     */
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

    /**
     * 忽略冲突保存实体
     *
     * @param entity 实体对象
     * @return 是否保存成功
     */
    default boolean saveIgnore(ENTITY entity) {

        if(null == entity) {
            return false;
        }
        return SqlHelper.retBool(getBaseMapper().insertIgnore(entity));
    }

    /**
     * 批量忽略冲突保存实体
     *
     * @param entities 实体集合
     * @return 保存成功的数量
     */
    default int batchSaveIgnore(Collection<ENTITY> entities) {
        if(CollUtil.isEmpty(entities)) {
            return 0;
        }
        return entities.stream()
            .map(this::saveIgnore)
            .mapToInt(e -> e ? 1 : 0)
            .sum();
    }

    /**
     * 按 ID 更新全部字段
     *
     * @param entity 实体对象
     * @return 是否更新成功
     */
    default boolean updateAllById(ENTITY entity) {
        if(null == entity) {
            return false;
        }
        return SqlHelper.retBool(getBaseMapper().updateAllById(entity));
    }

    /**
     * 按 ID 查询实体，无结果返回空 Opt
     *
     * @param id 主键 ID
     * @return 查询结果
     */
    default Opt<ENTITY> getByIdOpt(Serializable id) {
        if(null == id) {
            return Opt.empty();
        }
        return Opt.ofNullable(getById(id));
    }

    /**
     * 提取对象指定列的值
     *
     * @param o 源对象
     * @param column 列函数
     * @param <O> 源对象类型
     * @param <T> 列值类型
     * @return 列值
     */
    default <O, T> T convertValue(O o, SFunction<O, T> column) {
        if(null == o) {
            return null;
        }
        return column.apply(o);
    }

    /**
     * 批量提取对象集合指定列的值
     *
     * @param collection 源对象集合
     * @param column 列函数
     * @param <O> 源对象类型
     * @param <T> 列值类型
     * @return 列值集合
     */
    default <O, T> Set<T> convertValues(Collection<O> collection, SFunction<O, T> column) {
        if(CollUtil.isEmpty(collection)) {
            return CSet.of();
        }
        return CCollUtils.convertSet(collection, column::apply);
    }

    /**
     * 按实体指定列的值查询单个实体
     *
     * @param entity 实体对象
     * @param column 列函数
     * @return 查询结果
     */
    default ENTITY getByValue(ENTITY entity, SFunction<ENTITY, ?> column){
        if(null == entity) {
            return null;
        }
        return getByValue(column, convertValue(entity, column));
    }

    /**
     * 按指定列的值查询单个实体
     *
     * @param column 列函数
     * @param value 列值
     * @return 查询结果
     */
    default ENTITY getByValue(SFunction<ENTITY, ?> column, Object value){
        if(null == value) {
            return null;
        }
        return lambdaQuery()
                .eq(column, value)
                .one();
    }

    /**
     * 按实体指定列的值查询列表
     *
     * @param entity 实体对象
     * @param column 列函数
     * @return 查询结果列表
     */
    default List<ENTITY> listByValue(ENTITY entity, SFunction<ENTITY, ?> column){
        if(null == entity) {
            return CList.of();
        }
        return listByValue(column, convertValue(entity, column));
    }

    /**
     * 按指定列的值查询列表
     *
     * @param column 列函数
     * @param value 列值
     * @return 查询结果列表
     */
    default List<ENTITY> listByValue(SFunction<ENTITY, ?> column, Object value){
        if(null == value) {
            return CList.of();
        }
        return lambdaQuery()
                .eq(column, value)
                .list();
    }


    /**
     * 按实体指定列的值统计数量
     *
     * @param entity 实体对象
     * @param column 列函数
     * @return 数量
     */
    default Long countByValue(ENTITY entity, SFunction<ENTITY, ?> column){
        if(null == entity) {
            return 0L;
        }
        return countByValue(column, convertValue(entity, column));
    }

    /**
     * 按指定列的值统计数量
     *
     * @param column 列函数
     * @param value 列值
     * @return 数量
     */
    default Long countByValue(SFunction<ENTITY, ?> column, Object value){
        if(null == value) {
            return 0L;
        }
        return lambdaQuery()
                .eq(column, value)
                .count()
                .longValue();
    }

    /**
     * 按实体集合指定列的值统计数量
     *
     * @param collection 实体集合
     * @param column 列函数
     * @return 数量
     */
    default Long countByValues(Collection<ENTITY> collection, SFunction<ENTITY, ?> column){

        if(CollUtil.isEmpty(collection)) {
            return 0L;
        }

        val values = convertValues(collection, column);
        return countByValues(column, values);
    }

    /**
     * 按指定列的多个值统计数量
     *
     * @param column 列函数
     * @param values 列值集合
     * @return 数量
     */
    default Long countByValues(SFunction<ENTITY, ?> column, Collection<?> values){

        if(CollUtil.isEmpty(values)) {
            return 0L;
        }
        return lambdaQuery()
                .in(column, values)
                .count()
                .longValue();
    }


    /**
     * 按实体指定列的值更新
     *
     * @param entity 实体对象
     * @param column 列函数
     * @return 是否更新成功
     */
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

    /**
     * 按实体指定列的值删除
     *
     * @param entity 实体对象
     * @param column 列函数
     * @return 是否删除成功
     */
    default boolean removeByValue(ENTITY entity, SFunction<ENTITY, ?> column){
        if(null == entity) {
            return false;
        }
        return removeByValue(column, convertValue(entity, column));
    }

    /**
     * 按指定列的值删除
     *
     * @param column 列函数
     * @param value 列值
     * @return 是否删除成功
     */
    default boolean removeByValue(SFunction<ENTITY, ?> column, Object value){

        if(null == value) {
            return false;
        }
        return lambdaUpdate()
                .eq(column, value)
                .remove();
    }

    /**
     * 按实体集合指定列的值查询列表
     *
     * @param collection 实体集合
     * @param column 列函数
     * @return 查询结果列表
     */
    default List<ENTITY> listByValues(Collection<ENTITY> collection, SFunction<ENTITY, ?> column){

        if(CollUtil.isEmpty(collection)) {
            return CList.of();
        }
        val values = convertValues(collection, column);
        return listByValues(column, values);
    }

    /**
     * 按指定列的多个值查询列表
     *
     * @param column 列函数
     * @param values 列值集合
     * @return 查询结果列表
     */
    default List<ENTITY> listByValues(SFunction<ENTITY, ?> column, Collection<?> values){

        if(CollUtil.isEmpty(values)) {
            return CList.of();
        }
        return lambdaQuery()
                .in(column, values)
                .list();
    }

    /**
     * 按实体集合指定列的值批量删除
     *
     * @param collection 实体集合
     * @param column 列函数
     * @return 是否删除成功
     */
    default boolean removeByValues(Collection<ENTITY> collection, SFunction<ENTITY, ?> column){

        if(CollUtil.isEmpty(collection)) {
            return false;
        }
        val values = convertValues(collection, column);
        return removeByValues(column, values);
    }

    /**
     * 按指定列的多个值批量删除
     *
     * @param column 列函数
     * @param values 列值集合
     * @return 是否删除成功
     */
    default boolean removeByValues(SFunction<ENTITY, ?> column, Collection<?> values){

        if(CollUtil.isEmpty(values)) {
            return false;
        }
        return lambdaUpdate()
                .in(column, values)
                .remove();
    }

}
