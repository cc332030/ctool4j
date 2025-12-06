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

    default Integer countByBizId(String bizId){
        if(StrUtil.isBlank(bizId)) {
            return 0;
        }
        return countByValue(getBizIdColumn(), bizId);
    }

    default Integer countByBizId(BIZ_INTERFACE bizInterface){
        return countByBizId(getBizId(bizInterface));
    }

}
