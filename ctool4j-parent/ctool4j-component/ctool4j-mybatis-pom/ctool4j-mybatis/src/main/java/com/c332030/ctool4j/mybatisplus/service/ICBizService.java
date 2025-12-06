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

    default Long countByBizId(String bizId){
        if(StrUtil.isBlank(bizId)) {
            return 0L;
        }
        return countByValue(getBizIdColumn(), bizId);
    }

    default Long countByBizId(BIZ_INTERFACE bizInterface){
        if(bizInterface == null) {
            return 0L;
        }
        return countByBizId(getBizId(bizInterface));
    }

    default Long countByBizIds(Collection<String> bizIds){
        if(CollUtil.isEmpty(bizIds)) {
            return 0L;
        }
        return countByValues(getBizIdColumn(), bizIds);
    }

    default Long countByBizIds(List<? extends BIZ_INTERFACE> bizInterfaces){

        if(CollUtil.isEmpty(bizInterfaces)) {
            return 0L;
        }
        val bizIds = convertValues(bizInterfaces, this::getBizId);
        return countByBizIds(bizIds);
    }

}
