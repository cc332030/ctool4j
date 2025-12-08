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
public interface ICMainBizService<ENTITY extends MAIN_BIZ_INTERFACE, MAIN_BIZ_INTERFACE>
        extends ICService<ENTITY>, ICMainBizBaseService<ENTITY, MAIN_BIZ_INTERFACE> {

    default Integer countByMainBizId(String mainBizId){
        if(StrUtil.isBlank(mainBizId)) {
            return 0;
        }
        return countByValue(getMainBizIdColumn(), mainBizId);
    }

    default Integer countByMainBizId(MAIN_BIZ_INTERFACE mainBizInterface){
        if(mainBizInterface == null) {
            return 0;
        }
        return countByMainBizId(getMainBizId(mainBizInterface));
    }

    default Integer countByMainBizIds(Collection<String> mainBizIds){
        if(CollUtil.isEmpty(mainBizIds)) {
            return 0;
        }
        return countByValues(getMainBizIdColumn(), mainBizIds);
    }

    default Integer countByMainBizIds(List<? extends MAIN_BIZ_INTERFACE> mainBizInterfaces){

        if(CollUtil.isEmpty(mainBizInterfaces)) {
            return 0;
        }
        val mainBizIds = convertValues(mainBizInterfaces, this::getMainBizId);
        return countByMainBizIds(mainBizIds);
    }

}
