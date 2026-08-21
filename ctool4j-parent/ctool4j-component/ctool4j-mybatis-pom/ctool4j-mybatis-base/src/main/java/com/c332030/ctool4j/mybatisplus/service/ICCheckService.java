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
 * @see doc/design/mybatisplus/ICCheckService.adoc
 */
public interface ICCheckService<ENTITY> extends IService<ENTITY> {

    /**
     * 根据 ID 查询（ID 为空时返回 null）
     * @param id 主键
     * @return 实体
     */
    @Override
    default ENTITY getById(Serializable id) {
        if(null == id) {
            return null;
        }
        return IService.super.getById(id);
    }

    /**
     * 根据 ID 查询（ID 为空时返回 Optional.empty）
     * @param id 主键
     * @return 实体 Optional
     */
    @Override
    default Optional<ENTITY> getOptById(Serializable id) {
        if(null == id) {
            return Optional.empty();
        }
        return IService.super.getOptById(id);
    }

    /**
     * 根据 ID 列表查询（列表为空时返回空列表）
     * @param idList 主键列表
     * @return 实体列表
     */
    @Override
    default List<ENTITY> listByIds(Collection<? extends Serializable> idList) {
        if(CollUtil.isEmpty(idList)) {
            return CList.of();
        }
        return IService.super.listByIds(idList);
    }

}
