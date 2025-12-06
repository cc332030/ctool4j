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

    default Opt<ENTITY> getByBizIdOpt(String bizId){
        return Opt.ofNullable(getByBizId(bizId));
    }

    default ENTITY getByBizId(BIZ_INTERFACE bizInterface){

        val bizId = getBizId(bizInterface);
        if(StrUtil.isBlank(bizId)) {
            return null;
        }
        return getByBizId(bizId);
    }

    default Opt<ENTITY> getByBizIdOpt(BIZ_INTERFACE bizInterface){
        return Opt.ofNullable(getByBizId(bizInterface));
    }

    default List<ENTITY> listByBizId(String bizId){
        return listByValue(getBizIdColumn(), bizId);
    }

    default List<ENTITY> listByBizId(BIZ_INTERFACE bizInterface){

        val bizId = getBizId(bizInterface);
        if(StrUtil.isBlank(bizId)) {
            return CList.of();
        }
        return listByBizId(bizId);
    }

    default Long countByBizId(String bizId){
        return countByValue(getBizIdColumn(), bizId);
    }

    default Long countByBizId(BIZ_INTERFACE bizInterface){

        val bizId = getBizId(bizInterface);
        if(StrUtil.isBlank(bizId)) {
            return 0L;
        }
        return countByBizId(bizId);
    }

    default boolean removeByBizId(BIZ_INTERFACE bizInterface){
        val bizId = getBizId(bizInterface);
        if(StrUtil.isBlank(bizId)) {
            return false;
        }
        return removeByBizId(bizId);
    }

    default boolean removeByBizId(String bizId){
        return removeByValue(getBizIdColumn(), bizId);
    }

    default List<ENTITY> listByBizIds(Collection<String> bizIds){
        return listByValues(getBizIdColumn(), bizIds);
    }

    default List<ENTITY> listByBizIds(List<? extends BIZ_INTERFACE> bizInterfaces){

        val bizIds = convertValues(bizInterfaces, this::getBizId);
        if(CollUtil.isEmpty(bizIds)) {
            return CList.of();
        }
        return listByBizIds(bizIds);
    }

    default Long countByBizIds(Collection<String> bizIds){
        return countByValues(getBizIdColumn(), bizIds);
    }

    default Long countByBizIds(List<? extends BIZ_INTERFACE> bizInterfaces){

        val bizIds = convertValues(bizInterfaces, this::getBizId);
        if(CollUtil.isEmpty(bizIds)) {
            return 0L;
        }
        return countByBizIds(bizIds);
    }

    default boolean removeByBizIds(Collection<String> bizIds){
        return removeByValues(getBizIdColumn(), bizIds);
    }

    default boolean removeByBizIds(List<? extends BIZ_INTERFACE> bizInterfaces){

        val bizIds = convertValues(bizInterfaces, this::getBizId);
        if(CollUtil.isEmpty(bizIds)) {
            return false;
        }
        return removeByBizIds(bizIds);
    }

}
