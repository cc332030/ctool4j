package com.c332030.ctool4j.mybatisplus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c332030.ctool4j.mybatisplus.mapper.CBaseMapper;
import com.c332030.ctool4j.mybatisplus.service.ICService;

/**
 * <p>
 * Description: CAbstractServiceImpl
 * </p>
 *
 * @since 2025/11/27
 */
public abstract class CAbstractServiceImpl<M extends CBaseMapper<T>, T>
        extends ServiceImpl<M, T>
        implements ICService<T> {

}
