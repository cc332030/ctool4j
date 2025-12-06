package com.c332030.ctool4j.mybatisplus.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.val;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * Description: ICBizService
 * </p>
 *
 * @since 2025/12/6
 */
public interface ICBizService<ENTITY extends BIZ_INTERFACE, BIZ_INTERFACE>
        extends ICService<ENTITY>, ICBizBaseService<ENTITY, BIZ_INTERFACE>{

    default Integer countByBizId(String bizId){
        if(StrUtil.isBlank(bizId)) {
            return 0;
        }
        return countByValue(getBizIdColumn(), bizId);
    }

    default Integer countByBizId(BIZ_INTERFACE bizInterface){
        if(bizInterface == null) {
            return 0;
        }
        return countByBizId(getBizId(bizInterface));
    }

    default Integer countByBizIds(Collection<String> bizIds){
        if(CollUtil.isEmpty(bizIds)) {
            return 0;
        }
        return countByValues(getBizIdColumn(), bizIds);
    }

    default Integer countByBizIds(List<? extends BIZ_INTERFACE> bizInterfaces){

        if(CollUtil.isEmpty(bizInterfaces)) {
            return 0;
        }
        val bizIds = convertValues(bizInterfaces, this::getBizId);
        return countByBizIds(bizIds);
    }

}
