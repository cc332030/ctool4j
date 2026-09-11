package com.c332030.ctool4j.definition.interfaces;

import com.c332030.ctool4j.doc.annotation.CSchema;

/**
 * <p>
 * Description: ICMessage
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICMessage} 为 消息契约接口。</p>
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
 * @since 2025/12/30
 * @version 1.0
 */
public interface ICMessage {

    /**
     * 获取消息
     * @return 消息
     */
    @CSchema("提示信息")
    String getMessage();

}
