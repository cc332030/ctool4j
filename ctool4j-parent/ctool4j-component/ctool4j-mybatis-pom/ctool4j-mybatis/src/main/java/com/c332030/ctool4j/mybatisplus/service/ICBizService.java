package com.c332030.ctool4j.mybatisplus.service;

import cn.hutool.core.util.StrUtil;

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
        return countByBizId(getBizId(bizInterface));
    }

}
