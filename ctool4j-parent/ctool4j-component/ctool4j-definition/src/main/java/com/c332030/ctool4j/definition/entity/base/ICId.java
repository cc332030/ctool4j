package com.c332030.ctool4j.definition.entity.base;

import com.c332030.ctool4j.doc.annotation.CSchema;

import java.io.Serializable;

/**
 * <p>
 * Description: ICId
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICId&lt;T extends Serializable&gt;} 为通用主键契约接口：</p>
 * <ul>
 *   <li>{@code getId()}：获取主键</li>
 *   <li>{@code setId(T id)}：设置主键</li>
 * </ul>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>泛型 {@code T extends Serializable} 限定主键为可序列化类型。</li>
 *   <li>供各实体基类/具体主键类型接口（如 ICIntegerId/ICLongId/ICStringId）继承。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>无（接口仅声明契约）。</li>
 * </ul>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>作为带主键的实体基类公共契约。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>主键类型由子接口/实现决定；仅约定 getter/setter。</li>
 * </ul>
 *
 * @since 2025/5/26
 * @version 1.0
 */
public interface ICId<T extends Serializable> {

    /**
     * 获取主键
     * @return 主键
     */
    @CSchema("主键")
    T getId();

    /**
     * 设置主键
     * @param id 主键
     */
    void setId(T id);

}
