package com.c332030.ctool4j.mybatisplus.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.c332030.ctool4j.core.util.CList;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * Description: ICBizBaseService
 * </p>
 *
 * @since 2025/12/6
 */
public interface ICBizBaseService<ENTITY extends BIZ, BIZ>
        extends ICBaseService<ENTITY>{

    String getBizId(BIZ biz);

    SFunction<ENTITY, String> getBizIdColumn();

    default ENTITY getByBizId(String bizId){
        if(StrUtil.isBlank(bizId)) {
            return null;
        }
        return getByValue(getBizIdColumn(), bizId);
    }

    default ENTITY getByBizId(BIZ biz){
        if(Objects.isNull(biz)) {
            return null;
        }
        val bizId = getBizId(biz);
        return getByBizId(bizId);
    }

    default Opt<ENTITY> getByBizIdOpt(String bizId){
        return Opt.ofNullable(getByBizId(bizId));
    }

    default Opt<ENTITY> getByBizIdOpt(BIZ biz){
        return Opt.ofNullable(getByBizId(biz));
    }

    default List<ENTITY> listByBizId(String bizId){
        if(StrUtil.isBlank(bizId)) {
            return CList.of();
        }
        return listByValue(getBizIdColumn(), bizId);
    }

    default List<ENTITY> listByBizId(BIZ biz){

        if(null == biz) {
            return CList.of();
        }
        val bizId = getBizId(biz);
        return listByBizId(bizId);
    }

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

    default boolean removeByBizId(BIZ biz){

        if(null == biz) {
            return false;
        }
        val bizId = getBizId(biz);
        return removeByBizId(bizId);
    }

    default boolean removeByBizId(String bizId){
        if(StrUtil.isBlank(bizId)) {
            return false;
        }
        return removeByValue(getBizIdColumn(), bizId);
    }

    default List<ENTITY> listByBizIds(Collection<String> bizIds){
        if(CollUtil.isEmpty(bizIds)) {
            return CList.of();
        }
        return listByValues(getBizIdColumn(), bizIds);
    }

    default List<ENTITY> listByBizIds(List<? extends BIZ> bizList){

        if(CollUtil.isEmpty(bizList)) {
            return CList.of();
        }
        val bizIds = convertValues(bizList, this::getBizId);
        return listByBizIds(bizIds);
    }

    default boolean removeByBizIds(Collection<String> bizIds){
        if(CollUtil.isEmpty(bizIds)) {
            return false;
        }
        return removeByValues(getBizIdColumn(), bizIds);
    }

    default boolean removeByBizIds(List<? extends BIZ> bizList){

        if(CollUtil.isEmpty(bizList)) {
            return false;
        }
        val bizIds = convertValues(bizList, this::getBizId);
        return removeByBizIds(bizIds);
    }

}
