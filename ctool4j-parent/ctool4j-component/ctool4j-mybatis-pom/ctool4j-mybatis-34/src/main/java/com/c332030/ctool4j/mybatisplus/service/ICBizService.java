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
public interface ICBizService<ENTITY extends BIZ, BIZ>
        extends ICService<ENTITY>, ICBizBaseService<ENTITY, BIZ>{

    default Long countByBizId(String bizId){
        if(StrUtil.isBlank(bizId)) {
            return 0L;
        }
        return countByValue(getBizIdColumn(), bizId);
    }

    default Long countByBizId(BIZ biz){
        if(biz == null) {
            return 0L;
        }
        return countByBizId(getBizId(biz));
    }

    default Long countByBizIds(Collection<String> bizIds){
        if(CollUtil.isEmpty(bizIds)) {
            return 0L;
        }
        return countByValues(getBizIdColumn(), bizIds);
    }

    default Long countByBizIds(List<? extends BIZ> bizList){

        if(CollUtil.isEmpty(bizList)) {
            return 0L;
        }
        val bizIds = convertValues(bizList, this::getBizId);
        return countByBizIds(bizIds);
    }

}
