package com.c332030.ctool4j.mybatisplus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * Description: CBaseMapper
 * </p>
 *
 * @since 2025/11/27
 */
public interface CBaseMapper<T> extends BaseMapper<T> {

    int insertIgnore(T entity);

    int updateByIdIncludeNull(@Param(Constants.ENTITY) T entity);

}
