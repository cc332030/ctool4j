package com.c332030.ctool4j.mybatisplus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * Description: CBaseMapper
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CBaseMapper}：基础 Mapper 接口。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>继承 BaseMapper，提供插入忽略、按ID更新所有字段等自定义方法</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>默认继承 BaseMapper 能力</p>
 * <h2>适用范围</h2>
 * <p>Mapper 扩展</p>
 * <h2>不适用与边界场景</h2>
 * <p>依赖 SQL 注入器</p>
 * <h2>已知限制与取舍</h2>
 * <p>依赖 SQL 注入器</p>
 *
 * @since 2025/11/27
 * @version 1.0
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
