package com.c332030.ctool4j.mybatisplus.service.impl;

import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.mybatisplus.mapper.CBaseMapper;

/**
 * <p>
 * Description: CServiceImpl
 * </p>
 *
 * @since 2025/11/27
 */
public class CServiceImpl<M extends CBaseMapper<T>, T>
        extends CBaseServiceImpl<M, T> {

    @Override
    public Class<T> getEntityClass() {
        return CObjUtils.anyType(entityClass);
    }

}
