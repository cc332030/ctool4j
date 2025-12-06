package com.c332030.ctool4j.mybatisplus.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.c332030.ctool4j.core.util.CList;
import lombok.val;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * Description: ICBizBaseService
 * </p>
 *
 * @since 2025/12/6
 */
public interface ICBizBaseService<ENTITY extends BIZ_INTERFACE, BIZ_INTERFACE>
        extends ICBaseService<ENTITY>{

    String getBizId(BIZ_INTERFACE bizInterface);

    SFunction<ENTITY, String> getBizIdColumn();

    default ENTITY getByBizId(String bizId){
        return getByValue(getBizIdColumn(), bizId);
    }

    default ENTITY getByBizId(BIZ_INTERFACE bizInterface){

        val bizId = getBizId(bizInterface);
        if(StrUtil.isBlank(bizId)) {
            return null;
        }
        return getByBizId(bizId);
    }

    default Opt<ENTITY> getByBizIdOpt(String bizId){
        return Opt.ofNullable(getByBizId(bizId));
    }

    default Opt<ENTITY> getByBizIdOpt(BIZ_INTERFACE bizInterface){
        return Opt.ofNullable(getByBizId(bizInterface));
    }

    default List<ENTITY> listByBizId(String bizId){
        if(StrUtil.isBlank(bizId)) {
            return CList.of();
        }
        return listByValue(getBizIdColumn(), bizId);
    }

    default List<ENTITY> listByBizId(BIZ_INTERFACE bizInterface){

        if(null == bizInterface) {
            return CList.of();
        }
        val bizId = getBizId(bizInterface);
        return listByBizId(bizId);
    }

    default boolean removeByBizId(BIZ_INTERFACE bizInterface){

        if(null == bizInterface) {
            return false;
        }
        val bizId = getBizId(bizInterface);
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

    default List<ENTITY> listByBizIds(List<? extends BIZ_INTERFACE> bizInterfaces){

        if(CollUtil.isEmpty(bizInterfaces)) {
            return CList.of();
        }
        val bizIds = convertValues(bizInterfaces, this::getBizId);
        return listByBizIds(bizIds);
    }

    default boolean removeByBizIds(Collection<String> bizIds){
        if(CollUtil.isEmpty(bizIds)) {
            return false;
        }
        return removeByValues(getBizIdColumn(), bizIds);
    }

    default boolean removeByBizIds(List<? extends BIZ_INTERFACE> bizInterfaces){

        if(CollUtil.isEmpty(bizInterfaces)) {
            return false;
        }
        val bizIds = convertValues(bizInterfaces, this::getBizId);
        return removeByBizIds(bizIds);
    }

}
