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
 * Description: ICMainBizBaseService
 * </p>
 *
 * @since 2025/12/6
 */
public interface ICMainBizBaseService<ENTITY extends MAIN_BIZ_INTERFACE, MAIN_BIZ_INTERFACE>
        extends ICBaseService<ENTITY>{

    String getMainBizId(MAIN_BIZ_INTERFACE mainBizInterface);

    SFunction<ENTITY, String> getMainBizIdColumn();

    default ENTITY getByMainBizId(String mainBizId){
        if(StrUtil.isBlank(mainBizId)) {
            return null;
        }
        return getByValue(getMainBizIdColumn(), mainBizId);
    }

    default ENTITY getByMainBizId(MAIN_BIZ_INTERFACE mainBizInterface){
        if(Objects.isNull(mainBizInterface)) {
            return null;
        }
        val mainBizId = getMainBizId(mainBizInterface);
        return getByMainBizId(mainBizId);
    }

    default Opt<ENTITY> getByMainBizIdOpt(String mainBizId){
        return Opt.ofNullable(getByMainBizId(mainBizId));
    }

    default Opt<ENTITY> getByMainBizIdOpt(MAIN_BIZ_INTERFACE mainBizInterface){
        return Opt.ofNullable(getByMainBizId(mainBizInterface));
    }

    default List<ENTITY> listByMainBizId(String mainBizId){
        if(StrUtil.isBlank(mainBizId)) {
            return CList.of();
        }
        return listByValue(getMainBizIdColumn(), mainBizId);
    }

    default List<ENTITY> listByMainBizId(MAIN_BIZ_INTERFACE mainBizInterface){

        if(null == mainBizInterface) {
            return CList.of();
        }
        val mainBizId = getMainBizId(mainBizInterface);
        return listByMainBizId(mainBizId);
    }

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

    default boolean removeByMainBizId(MAIN_BIZ_INTERFACE mainBizInterface){

        if(null == mainBizInterface) {
            return false;
        }
        val mainBizId = getMainBizId(mainBizInterface);
        return removeByMainBizId(mainBizId);
    }

    default boolean removeByMainBizId(String mainBizId){
        if(StrUtil.isBlank(mainBizId)) {
            return false;
        }
        return removeByValue(getMainBizIdColumn(), mainBizId);
    }

    default List<ENTITY> listByMainBizIds(Collection<String> mainBizIds){
        if(CollUtil.isEmpty(mainBizIds)) {
            return CList.of();
        }
        return listByValues(getMainBizIdColumn(), mainBizIds);
    }

    default List<ENTITY> listByMainBizIds(List<? extends MAIN_BIZ_INTERFACE> mainBizInterfaces){

        if(CollUtil.isEmpty(mainBizInterfaces)) {
            return CList.of();
        }
        val mainBizIds = convertValues(mainBizInterfaces, this::getMainBizId);
        return listByMainBizIds(mainBizIds);
    }

    default boolean removeByMainBizIds(Collection<String> mainBizIds){
        if(CollUtil.isEmpty(mainBizIds)) {
            return false;
        }
        return removeByValues(getMainBizIdColumn(), mainBizIds);
    }

    default boolean removeByMainBizIds(List<? extends MAIN_BIZ_INTERFACE> mainBizInterfaces){

        if(CollUtil.isEmpty(mainBizInterfaces)) {
            return false;
        }
        val mainBizIds = convertValues(mainBizInterfaces, this::getMainBizId);
        return removeByMainBizIds(mainBizIds);
    }

}
