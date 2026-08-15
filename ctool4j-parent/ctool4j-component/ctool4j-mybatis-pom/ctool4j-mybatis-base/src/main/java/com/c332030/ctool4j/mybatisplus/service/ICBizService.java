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
 * Description: ICBizService
 * </p>
 *
 * @since 2025/12/6
 */
public interface ICBizService<ENTITY extends BIZ, BIZ>
        extends ICService<ENTITY> {

    /**
     * 获取业务 ID
     *
     * @param biz 业务对象
     * @return 业务 ID
     */
    String getBizId(BIZ biz);

    /**
     * 获取业务 ID 字段列
     *
     * @return 业务 ID 字段列
     */
    SFunction<ENTITY, String> getBizIdColumn();

    /**
     * 按业务 ID 查询单个实体
     *
     * @param bizId 业务 ID
     * @return 查询结果
     */
    default ENTITY getByBizId(String bizId){
        if(StrUtil.isBlank(bizId)) {
            return null;
        }
        return getByValue(getBizIdColumn(), bizId);
    }

    /**
     * 按业务对象查询单个实体
     *
     * @param biz 业务对象
     * @return 查询结果
     */
    default ENTITY getByBizId(BIZ biz){
        if(Objects.isNull(biz)) {
            return null;
        }
        val bizId = getBizId(biz);
        return getByBizId(bizId);
    }

    /**
     * 按业务 ID 查询单个实体，无结果返回空 Opt
     *
     * @param bizId 业务 ID
     * @return 查询结果
     */
    default Opt<ENTITY> getByBizIdOpt(String bizId){
        return Opt.ofNullable(getByBizId(bizId));
    }

    /**
     * 按业务对象查询单个实体，无结果返回空 Opt
     *
     * @param biz 业务对象
     * @return 查询结果
     */
    default Opt<ENTITY> getByBizIdOpt(BIZ biz){
        return Opt.ofNullable(getByBizId(biz));
    }

    /**
     * 按业务 ID 查询列表
     *
     * @param bizId 业务 ID
     * @return 查询结果列表
     */
    default List<ENTITY> listByBizId(String bizId){
        if(StrUtil.isBlank(bizId)) {
            return CList.of();
        }
        return listByValue(getBizIdColumn(), bizId);
    }

    /**
     * 按业务对象查询列表
     *
     * @param biz 业务对象
     * @return 查询结果列表
     */
    default List<ENTITY> listByBizId(BIZ biz){

        if(null == biz) {
            return CList.of();
        }
        val bizId = getBizId(biz);
        return listByBizId(bizId);
    }

    /**
     * 按业务 ID 更新实体
     *
     * @param entity 实体对象
     * @return 是否更新成功
     */
    default boolean updateByBizId(ENTITY entity){
        if(Objects.isNull(entity)) {
            return false;
        }
        val bizColumn = getBizIdColumn();
        val bizId = convertValue(entity, getBizIdColumn());
        if(StrUtil.isBlank(bizId)) {
            return false;
        }
        return lambdaUpdate()
                .eq(bizColumn, bizId)
                .update(entity);
    }

    /**
     * 按业务对象删除
     *
     * @param biz 业务对象
     * @return 是否删除成功
     */
    default boolean removeByBizId(BIZ biz){

        if(null == biz) {
            return false;
        }
        val bizId = getBizId(biz);
        return removeByBizId(bizId);
    }

    /**
     * 按业务 ID 删除
     *
     * @param bizId 业务 ID
     * @return 是否删除成功
     */
    default boolean removeByBizId(String bizId){
        if(StrUtil.isBlank(bizId)) {
            return false;
        }
        return removeByValue(getBizIdColumn(), bizId);
    }

    /**
     * 按业务 ID 集合查询列表
     *
     * @param bizIds 业务 ID 集合
     * @return 查询结果列表
     */
    default List<ENTITY> listByBizIds(Collection<String> bizIds){
        if(CollUtil.isEmpty(bizIds)) {
            return CList.of();
        }
        return listByValues(getBizIdColumn(), bizIds);
    }

    /**
     * 按业务对象集合查询列表
     *
     * @param bizList 业务对象集合
     * @return 查询结果列表
     */
    default List<ENTITY> listByBizIds(List<? extends BIZ> bizList){

        if(CollUtil.isEmpty(bizList)) {
            return CList.of();
        }
        val bizIds = convertValues(bizList, this::getBizId);
        return listByBizIds(bizIds);
    }

    /**
     * 按业务 ID 集合查询列表并转换
     *
     * @param bizIds 业务 ID 集合
     * @param converter 转换函数
     * @param <T> 转换结果类型
     * @return 转换结果
     */
    default <T> T listByBizIdsThenConvert(Collection<String> bizIds, CFunction<List<ENTITY>, T> converter){

        val list = listByBizIds(bizIds);
        return converter.apply(list);
    }

    /**
     * 按业务 ID 集合查询为业务 ID 到实体的映射
     *
     * @param bizIds 业务 ID 集合
     * @return 业务 ID 到实体的映射
     */
    default Map<String, ENTITY> listMapByBizIds(Collection<String> bizIds){

        if(CollUtil.isEmpty(bizIds)) {
            return CMap.of();
        }
        return listByBizIdsThenConvert(bizIds, list ->
                CCollUtils.toMap(list, this::getBizId));
    }

    /**
     * 按业务对象集合查询为业务 ID 到实体的映射
     *
     * @param bizList 业务对象集合
     * @return 业务 ID 到实体的映射
     */
    default Map<String, ENTITY> listMapByBizIds(List<? extends BIZ> bizList){
        val bizIds = convertValues(bizList, this::getBizId);
        return listMapByBizIds(bizIds);
    }

    /**
     * 按业务 ID 集合查询为业务 ID 到实体列表的分组映射
     *
     * @param bizIds 业务 ID 集合
     * @return 业务 ID 到实体列表的分组映射
     */
    default Map<String, List<ENTITY>> listGroupMapByBizIds(Collection<String> bizIds){

        if(CollUtil.isEmpty(bizIds)) {
            return CMap.of();
        }
        return listByBizIdsThenConvert(bizIds, list ->
                CCollUtils.groupingBy(list, this::getBizId));
    }

    /**
     * 按业务 ID 统计数量
     *
     * @param bizId 业务 ID
     * @return 数量
     */
    default Long countByBizId(String bizId){
        if(StrUtil.isBlank(bizId)) {
            return 0L;
        }
        return countByValue(getBizIdColumn(), bizId);
    }

    /**
     * 按业务对象统计数量
     *
     * @param biz 业务对象
     * @return 数量
     */
    default Long countByBizId(BIZ biz){
        if(biz == null) {
            return 0L;
        }
        return countByBizId(getBizId(biz));
    }

    /**
     * 按业务 ID 集合统计数量
     *
     * @param bizIds 业务 ID 集合
     * @return 数量
     */
    default Long countByBizIds(Collection<String> bizIds){
        if(CollUtil.isEmpty(bizIds)) {
            return 0L;
        }
        return countByValues(getBizIdColumn(), bizIds);
    }

    /**
     * 按业务对象集合统计数量
     *
     * @param bizList 业务对象集合
     * @return 数量
     */
    default Long countByBizIds(List<? extends BIZ> bizList){

        if(CollUtil.isEmpty(bizList)) {
            return 0L;
        }
        val bizIds = convertValues(bizList, this::getBizId);
        return countByBizIds(bizIds);
    }

    /**
     * 按业务 ID 集合删除
     *
     * @param bizIds 业务 ID 集合
     * @return 是否删除成功
     */
    default boolean removeByBizIds(Collection<String> bizIds){
        if(CollUtil.isEmpty(bizIds)) {
            return false;
        }
        return removeByValues(getBizIdColumn(), bizIds);
    }

    /**
     * 按业务对象集合删除
     *
     * @param bizList 业务对象集合
     * @return 是否删除成功
     */
    default boolean removeByBizIds(List<? extends BIZ> bizList){

        if(CollUtil.isEmpty(bizList)) {
            return false;
        }
        val bizIds = convertValues(bizList, this::getBizId);
        return removeByBizIds(bizIds);
    }

}
