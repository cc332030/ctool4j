package com.c332030.ctool4j.definition.interfaces;

/**
 * <p>
 * Description: ICOperate
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICOperate} 为 操作契约接口。</p>
 * <ul>
 *   <li>继承关系：extends ICEnumName</li>
 * </ul>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>契约接口，定义数据访问方法签名，供实体/结果类实现。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>无（接口仅声明契约，无逻辑）。</li>
 * </ul>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>作为 DTO/实体/枚举/结果对象的公共契约，统一数据访问语义。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>仅契约，无默认实现（除组合接口）。</li>
 * </ul>
 *
 * @since 2025/11/10
 * @version 1.0
 */
public interface ICOperate extends ICEnumName {

    /**
     * 获取操作名称（默认返回枚举名称）
     * @return 操作名称
     */
    default String getName() {
        return name();
    }

}
