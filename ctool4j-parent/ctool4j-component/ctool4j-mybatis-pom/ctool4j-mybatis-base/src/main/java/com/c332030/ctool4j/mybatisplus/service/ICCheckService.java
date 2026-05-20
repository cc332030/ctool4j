package com.c332030.ctool4j.mybatisplus.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.IService;
import com.c332030.ctool4j.core.util.CList;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * Description: ICCheckService
 * </p>
 *
 * @since 2026/5/20
 */
public interface ICCheckService<ENTITY> extends IService<ENTITY> {

    @Override
    default ENTITY getById(Serializable id) {
        if(null == id) {
            return null;
        }
        return IService.super.getById(id);
    }

    @Override
    default Optional<ENTITY> getOptById(Serializable id) {
        if(null == id) {
            return Optional.empty();
        }
        return IService.super.getOptById(id);
    }

    @Override
    default List<ENTITY> listByIds(Collection<? extends Serializable> idList) {
        if(CollUtil.isEmpty(idList)) {
            return CList.of();
        }
        return IService.super.listByIds(idList);
    }

}
