package com.c332030.ctool4j.interfaces;

import com.c332030.ctool4j.exception.CServletException;

import java.io.IOException;

/**
 * <p>
 * Description: CFilterChain
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CFilterChain} 为过滤器链的<b>抽象层</b>：对应 {@code javax.servlet.FilterChain} 与
 * {@code jakarta.servlet.FilterChain}，由两侧适配器把底层链包装成本接口。</p>
 * <ul>
 *   <li>{@code doFilter(request, response)}：放行到链上的下一个过滤器（或最终的目标 Servlet）</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>参数是抽象层类型</b>：调用方传 {@link CHttpRequest} / {@link CHttpResponse}，
 *   适配器负责解包成对应容器的请求/响应再交给底层链。</li>
 *   <li><b>异常</b>：底层 {@code ServletException} 包装为 {@link CServletException}（运行时异常），
 *   故本接口只声明 {@link IOException}。</li>
 *   <li><b>不提供"是否还有下一个过滤器"</b>：Servlet 规范的链不暴露该信息，本接口不臆造。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>无兜底：链为空（已是最后一个过滤器）时，底层容器会把请求交给目标资源，本接口不额外处理。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>{@link CFilter} 实现中放行请求；需要同时兼容 javax 与 jakarta 两套容器的过滤器代码。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要读取链上其余过滤器信息、或需要干预容器分派（{@code RequestDispatcher} 走
 *   {@link CRequestDispatcher}）的场景不适用。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>一次请求的链对象只能放行一次：重复调用与 Servlet 规范下的行为一致（由底层容器决定，通常是重复执行后续链）。</li>
 *   <li>解包要求请求/响应来自同侧适配器（{@code CHttpServletRequest}/{@code CHttpServletResponse}），
 *   否则抛 {@link IllegalArgumentException}。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.0
 */
public interface CFilterChain {

    /**
     * 放行到链上的下一个过滤器
     *
     * @param request  请求
     * @param response 响应
     * @throws IOException 读写请求或响应失败时
     */
    void doFilter(CHttpRequest request, CHttpResponse response) throws IOException;

}
