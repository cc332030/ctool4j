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
 * <h2>能力目录</h2>
 * <p>{@code ICCheckService}：检查服务接口。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>扩展 IService，提供按ID查询（空安全）</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>ID空返回 null/空Optional</p>
 * <h2>适用范围</h2>
 * <p>查询服务</p>
 * <h2>不适用与边界场景</h2>
 * <p>接口</p>
 * <h2>已知限制与取舍</h2>
 * <p>接口</p>
 *
 * @since 2026/5/20
 * @version 1.0
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
