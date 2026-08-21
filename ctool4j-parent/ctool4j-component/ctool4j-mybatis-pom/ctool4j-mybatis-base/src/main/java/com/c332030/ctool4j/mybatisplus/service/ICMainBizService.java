package com.c332030.ctool4j.mybatisplus.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.definition.function.CFunction;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * Description: ICMainBizService
 * </p>
 *
 * @since 2025/12/6
 * @see doc/design/mybatisplus/ICMainBizService.adoc
 */
public interface ICMainBizService<ENTITY extends MAIN_BIZ, MAIN_BIZ>
        extends ICService<ENTITY> {

    /**
     * 获取主业务 ID
     *
     * @param mainBiz 主业务对象
     * @return 主业务 ID
     */
    String getMainBizId(MAIN_BIZ mainBiz);

    /**
     * 获取主业务 ID 字段列
     *
     * @return 主业务 ID 字段列
     */
    SFunction<ENTITY, String> getMainBizIdColumn();

    /**
     * 按主业务 ID 查询单个实体
     *
     * @param mainBizId 主业务 ID
     * @return 查询结果
     */
    default ENTITY getByMainBizId(String mainBizId){
        if(StrUtil.isBlank(mainBizId)) {
            return null;
        }
        return getByValue(getMainBizIdColumn(), mainBizId);
    }

    /**
     * 按主业务对象查询单个实体
     *
     * @param mainBiz 主业务对象
     * @return 查询结果
     */
    default ENTITY getByMainBizId(MAIN_BIZ mainBiz){
        if(Objects.isNull(mainBiz)) {
            return null;
        }
        val mainBizId = getMainBizId(mainBiz);
        return getByMainBizId(mainBizId);
    }

    /**
     * 按主业务 ID 查询单个实体，无结果返回空 Opt
     *
     * @param mainBizId 主业务 ID
     * @return 查询结果
     */
    default Opt<ENTITY> getByMainBizIdOpt(String mainBizId){
        return Opt.ofNullable(getByMainBizId(mainBizId));
    }

    /**
     * 按主业务对象查询单个实体，无结果返回空 Opt
     *
     * @param mainBiz 主业务对象
     * @return 查询结果
     */
    default Opt<ENTITY> getByMainBizIdOpt(MAIN_BIZ mainBiz){
        return Opt.ofNullable(getByMainBizId(mainBiz));
    }

    /**
     * 按主业务 ID 查询列表
     *
     * @param mainBizId 主业务 ID
     * @return 查询结果列表
     */
    default List<ENTITY> listByMainBizId(String mainBizId){
        if(StrUtil.isBlank(mainBizId)) {
            return CList.of();
        }
        return listByValue(getMainBizIdColumn(), mainBizId);
    }

    /**
     * 按主业务对象查询列表
     *
     * @param mainBiz 主业务对象
     * @return 查询结果列表
     */
    default List<ENTITY> listByMainBizId(MAIN_BIZ mainBiz){

        if(null == mainBiz) {
            return CList.of();
        }
        val mainBizId = getMainBizId(mainBiz);
        return listByMainBizId(mainBizId);
    }

    /**
     * 按主业务 ID 统计数量
     *
     * @param mainBizId 主业务 ID
     * @return 数量
     */
    default Long countByMainBizId(String mainBizId){
        if(StrUtil.isBlank(mainBizId)) {
            return 0L;
        }
        return countByValue(getMainBizIdColumn(), mainBizId);
    }

    /**
     * 按主业务对象统计数量
     *
     * @param mainBiz 主业务对象
     * @return 数量
     */
    default Long countByMainBizId(MAIN_BIZ mainBiz){
        if(mainBiz == null) {
            return 0L;
        }
        return countByMainBizId(getMainBizId(mainBiz));
    }

    /**
     * 按主业务 ID 集合统计数量
     *
     * @param mainBizIds 主业务 ID 集合
     * @return 数量
     */
    default Long countByMainBizIds(Collection<String> mainBizIds){
        if(CollUtil.isEmpty(mainBizIds)) {
            return 0L;
        }
        return countByValues(getMainBizIdColumn(), mainBizIds);
    }

    /**
     * 按主业务对象集合统计数量
     *
     * @param mainBizList 主业务对象集合
     * @return 数量
     */
    default Long countByMainBizIds(List<? extends MAIN_BIZ> mainBizList){

        if(CollUtil.isEmpty(mainBizList)) {
            return 0L;
        }
        val mainBizIds = convertValues(mainBizList, this::getMainBizId);
        return countByMainBizIds(mainBizIds);
    }

    /**
     * 按主业务 ID 更新实体
     *
     * @param entity 实体对象
     * @return 是否更新成功
     */
    default boolean updateByMainBizId(ENTITY entity){
        if(Objects.isNull(entity)) {
            return false;
        }
        val bizColumn = getMainBizIdColumn();
        val mainBizId = convertValue(entity, getMainBizIdColumn());
        if(StrUtil.isBlank(mainBizId)) {
            return false;
        }
        return lambdaUpdate()
                .eq(bizColumn, mainBizId)
                .update(entity);
    }

    /**
     * 按主业务对象删除
     *
     * @param mainBiz 主业务对象
     * @return 是否删除成功
     */
    default boolean removeByMainBizId(MAIN_BIZ mainBiz){

        if(null == mainBiz) {
            return false;
        }
        val mainBizId = getMainBizId(mainBiz);
        return removeByMainBizId(mainBizId);
    }

    /**
     * 按主业务 ID 删除
     *
     * @param mainBizId 主业务 ID
     * @return 是否删除成功
     */
    default boolean removeByMainBizId(String mainBizId){
        if(StrUtil.isBlank(mainBizId)) {
            return false;
        }
        return removeByValue(getMainBizIdColumn(), mainBizId);
    }

    /**
     * 按主业务 ID 集合查询列表
     *
     * @param mainBizIds 主业务 ID 集合
     * @return 查询结果列表
     */
    default List<ENTITY> listByMainBizIds(Collection<String> mainBizIds){
        if(CollUtil.isEmpty(mainBizIds)) {
            return CList.of();
        }
        return listByValues(getMainBizIdColumn(), mainBizIds);
    }

    /**
     * 按主业务对象集合查询列表
     *
     * @param mainBizList 主业务对象集合
     * @return 查询结果列表
     */
    default List<ENTITY> listByMainBizIds(List<? extends MAIN_BIZ> mainBizList){

        if(CollUtil.isEmpty(mainBizList)) {
            return CList.of();
        }
        val mainBizIds = convertValues(mainBizList, this::getMainBizId);
        return listByMainBizIds(mainBizIds);
    }

    /**
     * 按主业务 ID 集合查询列表并转换
     *
     * @param mainBizIds 主业务 ID 集合
     * @param converter 转换函数
     * @param <T> 转换结果类型
     * @return 转换结果
     */
    default <T> T listByMainBizIdsThenConvert(Collection<String> mainBizIds, CFunction<List<ENTITY>, T> converter){

        val list = listByMainBizIds(mainBizIds);
        return converter.apply(list);
    }

    /**
     * 按主业务 ID 集合查询为主业务 ID 到实体的映射
     *
     * @param mainBizIds 主业务 ID 集合
     * @return 主业务 ID 到实体的映射
     */
    default Map<String, ENTITY> listMapByBizIds(Collection<String> mainBizIds){

        if(CollUtil.isEmpty(mainBizIds)) {
            return CMap.of();
        }
        return listByMainBizIdsThenConvert(mainBizIds, list ->
                CCollUtils.toMap(list, this::getMainBizId));
    }

    /**
     * 按主业务对象集合查询为主业务 ID 到实体的映射
     *
     * @param mainBizList 主业务对象集合
     * @return 主业务 ID 到实体的映射
     */
    default Map<String, ENTITY> listMapByMainBizIds(List<? extends MAIN_BIZ> mainBizList){
        val mainBizIds = convertValues(mainBizList, this::getMainBizId);
        return listMapByBizIds(mainBizIds);
    }

    /**
     * 按主业务 ID 集合查询为主业务 ID 到实体列表的分组映射
     *
     * @param mainBizIds 主业务 ID 集合
     * @return 主业务 ID 到实体列表的分组映射
     */
    default Map<String, List<ENTITY>> listGroupMapByBizIds(Collection<String> mainBizIds){

        if(CollUtil.isEmpty(mainBizIds)) {
            return CMap.of();
        }
        return listByMainBizIdsThenConvert(mainBizIds, list ->
                CCollUtils.groupingBy(list, this::getMainBizId));
    }

    /**
     * 按主业务 ID 集合删除
     *
     * @param mainBizIds 主业务 ID 集合
     * @return 是否删除成功
     */
    default boolean removeByMainBizIds(Collection<String> mainBizIds){
        if(CollUtil.isEmpty(mainBizIds)) {
            return false;
        }
        return removeByValues(getMainBizIdColumn(), mainBizIds);
    }

    /**
     * 按主业务对象集合删除
     *
     * @param mainBizList 主业务对象集合
     * @return 是否删除成功
     */
    default boolean removeByMainBizIds(List<? extends MAIN_BIZ> mainBizList){

        if(CollUtil.isEmpty(mainBizList)) {
            return false;
        }
        val mainBizIds = convertValues(mainBizList, this::getMainBizId);
        return removeByMainBizIds(mainBizIds);
    }

}
