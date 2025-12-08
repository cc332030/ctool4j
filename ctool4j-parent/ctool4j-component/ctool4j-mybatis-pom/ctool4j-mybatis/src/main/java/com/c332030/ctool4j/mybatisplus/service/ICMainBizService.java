package com.c332030.ctool4j.mybatisplus.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.val;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * Description: ICMainBizService
 * </p>
 *
 * @since 2025/12/6
 */
public interface ICMainBizService<ENTITY extends MAIN_BIZ, MAIN_BIZ>
        extends ICService<ENTITY>, ICMainBizBaseService<ENTITY, MAIN_BIZ> {

    default Long countByMainBizId(String mainBizId){
        if(StrUtil.isBlank(mainBizId)) {
            return 0L;
        }
        return countByValue(getMainBizIdColumn(), mainBizId);
    }

    default Long countByMainBizId(MAIN_BIZ mainBiz){
        if(mainBiz == null) {
            return 0L;
        }
        return countByMainBizId(getMainBizId(mainBiz));
    }

    default Long countByMainBizIds(Collection<String> mainBizIds){
        if(CollUtil.isEmpty(mainBizIds)) {
            return 0L;
        }
        return countByValues(getMainBizIdColumn(), mainBizIds);
    }

    default Long countByMainBizIds(List<? extends MAIN_BIZ> mainBizs){

        if(CollUtil.isEmpty(mainBizs)) {
            return 0L;
        }
        val mainBizIds = convertValues(mainBizs, this::getMainBizId);
        return countByMainBizIds(mainBizIds);
    }

}
