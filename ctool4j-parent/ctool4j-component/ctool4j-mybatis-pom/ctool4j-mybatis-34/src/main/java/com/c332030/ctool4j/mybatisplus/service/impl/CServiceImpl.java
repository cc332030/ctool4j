package com.c332030.ctool4j.mybatisplus.service.impl;

import com.c332030.ctool4j.mybatisplus.mapper.CBaseMapper;

/**
 * <p>
 * Description: CServiceImpl
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CServiceImpl}：服务实现。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>实现 ICService 的默认服务逻辑</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>服务实现</p>
 * <h2>不适用与边界场景</h2>
 * <p>实现 ICService</p>
 * <h2>已知限制与取舍</h2>
 * <p>实现 ICService</p>
 *
 * @since 2025/11/27
 * @version 1.0
 */
public abstract class CServiceImpl<M extends CBaseMapper<T>, T>
        extends CBaseServiceImpl<M, T> {

}
