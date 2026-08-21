package com.c332030.ctool4j.mybatisplus.service.impl;

import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.mybatisplus.mapper.CBaseMapper;

/**
 * <p>
 * Description: CServiceImpl
 * </p>
 *
 * @since 2025/11/27
 * @see doc/design/mybatisplus/CServiceImpl.adoc
 */
public abstract class CServiceImpl<M extends CBaseMapper<T>, T>
        extends CBaseServiceImpl<M, T> {

    /**
     * 获取实体类类型
     *
     * @return 实体类
     */
    @Override
    public Class<T> getEntityClass() {
        return CObjUtils.anyType(entityClass);
    }

}
