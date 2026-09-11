package com.c332030.ctool4j.core.interfaces;

import com.c332030.ctool4j.definition.interfaces.ICText;

/**
 * <p>
 * Description: 日志来源接口
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICSource} 为日志来源接口，继承 {@code ICText}，表示请求/日志的来源（如 MVC、Feign）。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>日志来源枚举/类实现 {@code ICSource}，统一描述来源（{@code getText()}）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>为标记接口，无默认行为；具体来源由实现类定义。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>通过继承 ICText 统一来源描述的文本语义。</li>
 * </ul>
 *
 * @author c332030
 * @since 1.0
 * @version 1.0
 */
public interface ICSource extends ICText {

}
