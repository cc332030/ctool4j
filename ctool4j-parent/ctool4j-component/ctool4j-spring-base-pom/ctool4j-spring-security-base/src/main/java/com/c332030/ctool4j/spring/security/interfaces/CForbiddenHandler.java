package com.c332030.ctool4j.spring.security.interfaces;

import com.c332030.ctool4j.interfaces.CHttpRequest;
import com.c332030.ctool4j.interfaces.CHttpResponse;

import org.springframework.security.access.AccessDeniedException;

/**
 * <p>
 * Description: CForbiddenHandler
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CForbiddenHandler} 为"已认证但无权限"处理的<b>抽象契约</b>：对应 Spring Security 的
 * {@code org.springframework.security.web.access.AccessDeniedHandler}（{@code handle}），
 * 但请求/响应参数换成抽象层的 {@link CHttpRequest} / {@link CHttpResponse}。</p>
 * <ul>
 *   <li>{@code handle(request, response, accessDeniedException)}：访问被拒绝时输出拒绝响应（通常是 403）</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>为什么要这一层</b>：Spring Security 的 {@code AccessDeniedHandler#handle} 签名里写死了
 *   Servlet 的 {@code HttpServletRequest}/{@code HttpServletResponse}（javax 与 jakarta 两套）；
 *   本接口换成抽象层类型，由两侧的 {@code CAccessDeniedHandler} 实现 Spring 接口并委托。</li>
 *   <li><b>保留 {@link AccessDeniedException}</b>：该类型属 Spring Security、与 Servlet 包无关，直接沿用。</li>
 *   <li><b>无返回值</b>：与 Spring 的 {@code handle} 一致——拒绝响应由实现直接写到响应上。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>无兜底：实现须自行保证响应已写出、状态码已设置。</li>
 *   <li>响应已提交时写响应会抛容器异常，由实现按业务需要处理。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要在 javax 与 jakarta 两套容器间可切换的访问拒绝处理；使用方直接用两侧模块提供的
 *   {@code CAccessDeniedHandler}（同名类）或实现本接口。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要跳转登录页、写 Cookie 等依赖容器专有能力的场景不适用，需直接实现 Spring Security 的
 *   {@code AccessDeniedHandler}。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>本接口不继承 Spring 的 {@code AccessDeniedHandler}，保持抽象层方法签名唯一。</li>
 *   <li>与 {@link CUnauthorizedHandler} 分工明确：未认证 → 本类不处理；已认证无权限 → 本类处理。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.0
 */
public interface CForbiddenHandler {

    /**
     * 处理访问被拒绝：输出拒绝响应（通常是 403）
     *
     * @param request               请求
     * @param response              响应
     * @param accessDeniedException 触发本次拒绝的权限异常
     */
    void handle(CHttpRequest request, CHttpResponse response, AccessDeniedException accessDeniedException);

}
