package com.c332030.ctool4j.definition.interfaces;

import com.c332030.ctool4j.doc.annotation.CSchema;

/**
 * <p>
 * Description: ICUsername
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICUsername} 为 用户名契约接口。</p>
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
 * @since 2026/1/24
 * @version 1.0
 */
public interface ICUsername {

    /**
     * 获取用户名
     * @return 用户名
     */
    @CSchema("用户名")
    String getUsername();

    /**
     * 设置用户名
     * @param username 用户名
     */
    void setUsername(String username);

}
