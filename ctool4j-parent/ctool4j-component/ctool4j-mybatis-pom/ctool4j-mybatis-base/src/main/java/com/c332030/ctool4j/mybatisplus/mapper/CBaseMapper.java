package com.c332030.ctool4j.mybatisplus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * Description: CBaseMapper
 * </p>
 *
 * @since 2025/11/27
 */
public interface CBaseMapper<T> extends BaseMapper<T> {

    int insertIgnore(T entity);

    int updateByIdIncludeNull(T entity);

}
