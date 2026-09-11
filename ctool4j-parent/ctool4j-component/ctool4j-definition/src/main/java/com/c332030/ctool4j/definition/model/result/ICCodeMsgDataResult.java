package com.c332030.ctool4j.definition.model.result;

import com.c332030.ctool4j.definition.interfaces.ICMsgAdapter;

/**
 * <p>
 * Description: ICCodeMsgDataResult
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICCodeMsgDataResult} 为编码消息数据结果（简写）契约接口。</p>
 * <ul>
 *   <li>组合语义：组合基础结果与消息适配语义</li>
 *   <li>继承关系：extends ICBaseResult&lt;CODE, DATA&gt;, ICMsgAdapter</li>
 * </ul>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>契约接口，组合编码/消息/数据等子契约，供各类结果对象实现。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>无（接口仅声明契约）。</li>
 * </ul>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>作为业务结果对象的公共契约（如含编码、消息、数据的响应体）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>仅契约；{@code ICCodeMessageDataResult} 标注 {@code @CJsonLog}（日志脱敏）。</li>
 * </ul>
 *
 * @author c332030
 * @since 2025/12/9
 * @version 1.0
 */
public interface ICCodeMsgDataResult<CODE, DATA> extends ICBaseResult<CODE, DATA>, ICMsgAdapter {

}
