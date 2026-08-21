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
 * @see "doc/design/mybatisplus/CBaseMapper.adoc"
 */
public interface CBaseMapper<T> extends BaseMapper<T> {

    /**
     * 插入或忽略（存在则忽略）
     * @param entity 实体
     * @return 受影响行数
     */
    int insertIgnore(T entity);

    /**
     * 根据 ID 更新所有字段
     * @param entity 实体
     * @return 受影响行数
     */
    int updateAllById(@Param(Constants.ENTITY) T entity);

}
