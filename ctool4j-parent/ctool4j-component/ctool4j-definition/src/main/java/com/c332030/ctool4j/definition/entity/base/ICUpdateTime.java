package com.c332030.ctool4j.definition.entity.base;

import com.c332030.ctool4j.doc.annotation.CSchema;

import java.util.Date;

/**
 * <p>
 * Description: ICUpdateTime
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICUpdateTime} 为更新时间契约接口。</p>
 * <ul>
 *   <li>{@code getUpdateTime()}/{@code setUpdateTime(Date)}：更新时间</li>
 * </ul>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>契约接口，定义实体审计字段（创建/更新人、时间、主键）的访问方法，供实体基类实现。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>无（接口仅声明契约，无逻辑）。</li>
 * </ul>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>作为带审计字段/主键的实体基类公共契约。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>仅契约；主键类型由具体接口（Integer/Long/String）固定。</li>
 * </ul>
 *
 * @since 2025/5/26
 * @version 1.0
 */
public interface ICUpdateTime {

    /**
     * 获取更新时间
     * @return 更新时间
     */
    @CSchema("更新时间")
    Date getUpdateTime();

    /**
     * 设置更新时间
     * @param updateTime 更新时间
     */
    void setUpdateTime(Date updateTime);

}
