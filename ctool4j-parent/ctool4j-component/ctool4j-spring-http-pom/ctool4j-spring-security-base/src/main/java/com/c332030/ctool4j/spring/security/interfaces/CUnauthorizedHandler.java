package com.c332030.ctool4j.spring.security.interfaces;

import com.c332030.ctool4j.interfaces.CHttpRequest;
import com.c332030.ctool4j.interfaces.CHttpResponse;

import org.springframework.security.core.AuthenticationException;

/**
 * <p>
 * Description: CUnauthorizedHandler
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CUnauthorizedHandler} 为"未认证访问"处理的<b>抽象契约</b>：对应 Spring Security 的
 * {@code org.springframework.security.web.AuthenticationEntryPoint}（{@code commence}），
 * 但请求/响应参数换成抽象层的 {@link CHttpRequest} / {@link CHttpResponse}。</p>
 * <ul>
 *   <li>{@code handle(request, response, authenticationException)}：未认证访问时输出拒绝响应（通常是 401）</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>为什么要这一层</b>：Spring Security 的 {@code AuthenticationEntryPoint#commence} 签名里写死了
 *   Servlet 的 {@code HttpServletRequest}/{@code HttpServletResponse}（javax 与 jakarta 两套），实现方无法回避；
 *   本接口把参数换成抽象层类型，由两侧的 {@code CAuthenticationEntryPoint} 实现 Spring 接口、把参数转换后委托过来。</li>
 *   <li><b>保留 {@link AuthenticationException}</b>：该类型属 Spring Security、与 Servlet 包无关，直接沿用，
 *   以便按异常类型决定提示内容。</li>
 *   <li><b>无返回值</b>：与 Spring 的 {@code commence} 一致——拒绝响应由实现直接写到响应上。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>无兜底：实现须自行保证"响应已写出/已设置状态码"，本接口不做状态跟踪。</li>
 *   <li>响应已提交时写响应会抛容器异常（{@code IllegalStateException} 一类），由实现按业务需要处理。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要在 javax 与 jakarta 两套容器间可切换的未认证处理；使用方直接用两侧模块提供的
 *   {@code CAuthenticationEntryPoint}（同名类）或实现本接口。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要重定向到登录页等依赖容器专有能力（会话、Cookie 写入）的场景：抽象层公共面不含这些能力，
 *   需直接实现 Spring Security 的 {@code AuthenticationEntryPoint}。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>本接口不继承 Spring 的 {@code AuthenticationEntryPoint}，理由与 http 侧一致：
 *   继承会把含 Servlet 类型的方法签名带进来，与抽象层方法形成两套入口。</li>
 *   <li>只承载"未认证"这一个职责；已认证但无权限的拒绝由 {@link CForbiddenHandler} 承担。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.0
 */
public interface CUnauthorizedHandler {

    /**
     * 处理未认证访问：输出拒绝响应（通常是 401）
     *
     * @param request                 请求
     * @param response                响应
     * @param authenticationException 触发本次拒绝的认证异常
     */
    void handle(CHttpRequest request, CHttpResponse response, AuthenticationException authenticationException);

}
