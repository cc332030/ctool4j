package com.c332030.ctool4j.core.enums;

import com.c332030.ctool4j.core.interfaces.ICSource;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Description: 日志来源枚举
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CLogSource} 为日志来源枚举，实现 {@code ICSource}，定义日志来源标识：</p>
 * <ul>
 *   <li>{@code MVC}（"mvc"）：服务端 MVC 接口收到的请求</li>
 *   <li>{@code FEIGN}（"feign"）：Feign 客户端发起的请求</li>
 * </ul>
 * <p>提供 {@code getText()} 获取来源标识（日志最前面 {@code [source]} 前缀）。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>请求日志的来源标记（MVC/Feign），统一 {@code [source]} 前缀。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>仅两类来源；新增来源需扩展枚举。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>标识为固定文本（mvc/feign），作为日志前缀约定。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>来源标识语义</b></p>
 * <ul>
 *   <li>每个来源对应一个文本标识，作为日志前缀区分请求来源（服务端接口 vs 客户端调用）。</li>
 * </ul>
 *
 * @author c332030
 * @since 1.0
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum CLogSource implements ICSource {

    /**
     * 服务端 MVC 接口收到的请求
     */
    MVC("mvc"),

    /**
     * feign 客户端发起的请求
     */
    FEIGN("feign"),

    ;

    /**
     * 来源标识（日志最前面 [source] 前缀）
     */
    private final String text;

}
