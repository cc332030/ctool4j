package com.c332030.ctool4j.web.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

/**
 * <p>
 * Description: ICHandlerInterceptor
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICHandlerInterceptor} 为项目统一的服务端处理器拦截器接口，继承 Spring {@code HandlerInterceptor}， 无额外方法（空接口标记）。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>无</td>
 *     <td>空接口标记，无行为</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>项目内所有处理器拦截器实现该接口，由 {@code CWebMvcConfigurer} 自动收集注册。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>继承 {@code HandlerInterceptor} 默认三个方法均可按需覆写。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>统一标记</b></p>
 * <ul>
 *   <li>作为项目内拦截器的统一类型标记，便于 {@code CWebMvcConfigurer} 收集容器中所有实现并统一注册。</li>
 * </ul>
 *
 * @since 2025/9/28
 * @version 1.0
 */
public interface ICHandlerInterceptor extends HandlerInterceptor {

}
