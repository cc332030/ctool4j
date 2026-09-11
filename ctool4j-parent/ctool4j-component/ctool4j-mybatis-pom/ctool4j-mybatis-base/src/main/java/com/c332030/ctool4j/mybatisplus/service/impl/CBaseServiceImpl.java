package com.c332030.ctool4j.mybatisplus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c332030.ctool4j.mybatisplus.mapper.CBaseMapper;
import com.c332030.ctool4j.mybatisplus.service.ICService;

/**
 * <p>
 * Description: CBaseServiceImpl
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CBaseServiceImpl}：基础服务实现。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>实现 ICService，提供分页/查询/业务ID等公共实现</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>默认实现</p>
 * <h2>适用范围</h2>
 * <p>服务基类</p>
 * <h2>不适用与边界场景</h2>
 * <p>实现 ICService</p>
 * <h2>已知限制与取舍</h2>
 * <p>实现 ICService</p>
 *
 * @since 2025/11/27
 * @version 1.0
 */
public abstract class CBaseServiceImpl<M extends CBaseMapper<T>, T>
        extends ServiceImpl<M, T>
        implements ICService<T> {

}
