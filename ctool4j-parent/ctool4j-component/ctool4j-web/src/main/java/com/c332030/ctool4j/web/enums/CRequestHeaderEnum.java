package com.c332030.ctool4j.web.enums;

import com.c332030.ctool4j.core.interfaces.ICRequestHeader;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Description: CRequestHeaderEnum
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CRequestHeaderEnum} 为请求头枚举，实现 {@code ICRequestHeader}，定义 7 种请求头 （AUTHORIZATION/ACCEPT_LANGUAGE/ACCEPT/X_REAL_IP/X_TRACE_ID/X_TENANT_ID/X_USER_ID），各带描述。</p>
 * <p>继承默认行为：{@code getHeaderName()}（下划线转中划线）、{@code getText()}。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>请求头字段的标准化枚举（鉴权/语言/追踪 id 等）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>描述为中文文本。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>Header 名推导</b></p>
 * <ul>
 *   <li>经 {@code ICRequestHeader.getHeaderName()} 默认实现（{@code upperUnderscoreToHeaderName}）转</li>
 *   <li>{@code X_TRACE_ID} → {@code X-Trace-Id} 等。</li>
 * </ul>
 *
 * @author c332030
 * @since 2024/3/21
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum CRequestHeaderEnum implements ICRequestHeader {

    AUTHORIZATION("鉴权"),
    ACCEPT_LANGUAGE("语言"),
    ACCEPT("内容格式"),

    X_REAL_IP("真实IP"),
    X_TRACE_ID("链路追踪ID"),
    X_TENANT_ID("租户ID"),
    X_USER_ID("用户ID"),

    ;

    /**
     * 描述
     */
    private final String text;

}
