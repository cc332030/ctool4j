package com.c332030.ctool4j.definition.entity.base;

/**
 * <p>
 * Description: ICLongId
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICLongId} 为长整型主键契约接口。 主键类型固定为 Long</p>
 * <ul>
 *   <li>继承关系：extends ICId&lt;Long&gt;</li>
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
public interface ICLongId extends ICId<Long> {

}
