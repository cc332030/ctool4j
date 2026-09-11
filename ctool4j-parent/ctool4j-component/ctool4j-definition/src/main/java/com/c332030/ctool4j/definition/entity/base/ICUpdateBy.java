package com.c332030.ctool4j.definition.entity.base;

import com.c332030.ctool4j.doc.annotation.CSchema;

/**
 * <p>
 * Description: ICUpdateBy
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICUpdateBy} 为更新人契约接口。</p>
 * <ul>
 *   <li>{@code getUpdateById()}/{@code setUpdateById(Long)}：更新人 ID；- {@code getUpdateBy()}/{@code setUpdateBy(String)}：更新人</li>
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
public interface ICUpdateBy {

    /**
     * 获取更新人ID
     * @return 更新人ID
     */
    @CSchema("更新人ID")
    Long getUpdateById();

    /**
     * 设置更新人ID
     * @param updateById 更新人ID
     */
    void setUpdateById(Long updateById);

    /**
     * 获取更新人
     * @return 更新人
     */
    @CSchema("更新人")
    String getUpdateBy();

    /**
     * 设置更新人
     * @param updateBy 更新人
     */
    void setUpdateBy(String updateBy);

}
