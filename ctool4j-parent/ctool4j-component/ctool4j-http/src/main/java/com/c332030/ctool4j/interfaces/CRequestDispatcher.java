package com.c332030.ctool4j.interfaces;

import com.c332030.ctool4j.exception.CServletException;

import java.io.IOException;

/**
 * <p>
 * Description: CRequestDispatcher
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CRequestDispatcher} 为请求转发器的<b>抽象层</b>：对应 {@code javax.servlet.RequestDispatcher} 与
 * {@code jakarta.servlet.RequestDispatcher}，由两侧适配器把底层转发器包装成本接口；
 * 取用入口是 {@link CHttpRequest#getRequestDispatcher(String)}。</p>
 * <ul>
 *   <li>{@code forward(request, response)}：把请求转发给目标资源（目标资源完全接管响应）</li>
 *   <li>{@code include(request, response)}：把目标资源的输出并入当前响应（当前响应头与状态保留）</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>取用走请求接口</b>：转发器由容器按路径创建，故入口放在 {@link CHttpRequest#getRequestDispatcher(String)}，
 *   本接口只声明转发动作，不提供构造函数。</li>
 *   <li><b>异常</b>：底层 {@code ServletException} 包装为 {@link CServletException}（运行时异常），
 *   故本接口只声明 {@link IOException}。</li>
 *   <li><b>只保留 forward / include</b>：{@code RequestDispatcher} 仅此两个动作。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>无兜底：路径非法或容器无法创建转发器时，{@link CHttpRequest#getRequestDispatcher(String)} 返回 {@code null}，
 *   调用方须自行判空（与 Servlet 规范一致）。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要在过滤器/拦截器中把请求转交给其它资源，且代码要同时兼容 javax 与 jakarta 两套容器的场景。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>{@code forward} 调用前若响应已提交则抛 {@code IllegalStateException}（容器语义），本接口不拦截、不包装。</li>
 *   <li>响应已被包装（如自定义 ResponseWrapper）时的转发语义由容器决定，本接口不做额外处理。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>解包要求请求/响应来自同侧适配器（{@code CHttpServletRequest}/{@code CHttpServletResponse}），
 *   否则抛 {@link IllegalArgumentException}。</li>
 *   <li>只暴露路径取用（{@link CHttpRequest#getRequestDispatcher(String)}），不提供按名称（{@code getNamedDispatcher}）取用
 *   ——当前无使用点，需要时再补。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.0
 */
public interface CRequestDispatcher {

    /**
     * 把请求转发给目标资源；目标资源完全接管响应
     *
     * @param request  请求
     * @param response 响应
     * @throws IOException 读写请求或响应失败时
     */
    void forward(CHttpRequest request, CHttpResponse response) throws IOException;

    /**
     * 把目标资源的输出并入当前响应；当前响应的头与状态码保留
     *
     * @param request  请求
     * @param response 响应
     * @throws IOException 读写请求或响应失败时
     */
    void include(CHttpRequest request, CHttpResponse response) throws IOException;

}
