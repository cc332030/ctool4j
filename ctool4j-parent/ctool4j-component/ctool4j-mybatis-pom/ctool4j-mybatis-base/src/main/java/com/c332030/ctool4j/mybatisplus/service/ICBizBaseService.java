package com.c332030.ctool4j.mybatisplus.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CList;
import lombok.val;

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

    default ENTITY getByBizId(BIZ_INTERFACE bizInterface){

        val bizId = getBizId(bizInterface);
        if(StrUtil.isBlank(bizId)) {
            return null;
        }
        return getByBizId(bizId);
    }

    default List<ENTITY> listByBizId(BIZ_INTERFACE bizInterface){

        val bizId = getBizId(bizInterface);
        if(StrUtil.isBlank(bizId)) {
            return CList.of();
        }
        return listByBizId(bizId);
    }

    default List<ENTITY> listByBizIds(List<BIZ_INTERFACE> bizInterfaces){

        val bizIds = CCollUtils.convert(bizInterfaces, this::getBizId);
        if(CollUtil.isEmpty(bizIds)) {
            return CList.of();
        }
        return listByBizIds(bizIds);
    }

}
