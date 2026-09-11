package com.c332030.ctool4j.definition.entity.base;

import com.c332030.ctool4j.doc.annotation.CSchema;

/**
 * <p>
 * Description: ICCreateBy
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICCreateBy} 为创建人契约接口。</p>
 * <ul>
 *   <li>{@code getCreateById()}/{@code setCreateById(Long)}：创建人 ID；- {@code getCreateBy()}/{@code setCreateBy(String)}：创建人</li>
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
 * @since 2025/12/6
 * @version 1.0
 */
public interface ICCreateBy {

    /**
     * 获取创建人ID
     * @return 创建人ID
     */
    @CSchema("创建人ID")
    Long getCreateById();

    /**
     * 设置创建人ID
     * @param createById 创建人ID
     */
    void setCreateById(Long createById);

    /**
     * 获取创建人
     * @return 创建人
     */
    @CSchema("创建人")
    String getCreateBy();

    /**
     * 设置创建人
     * @param createBy 创建人
     */
    void setCreateBy(String createBy);

}
