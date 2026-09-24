package com.c332030.ctool4j.interfaces;

import com.c332030.ctool4j.exception.CServletException;

import java.io.IOException;

/**
 * <p>
 * Description: CFilter
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CFilter} 为 Servlet 过滤器的<b>抽象层</b>：对应 {@code javax.servlet.Filter} 与
 * {@code jakarta.servlet.Filter}，由两侧适配器实现各自的 Filter 接口并委托到本接口，
 * 使用方只实现本接口、不接触任一 Servlet 包。</p>
 * <ul>
 *   <li>{@code doFilter(request, response, chain)}：过滤请求；处理完可调用 {@link CFilterChain#doFilter} 放行，
 *   也可直接结束请求（如自行写出响应）</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>只保留 doFilter</b>：容器生命周期回调 {@code init(FilterConfig)} 与 {@code destroy()} 不在本接口暴露——
 *   {@code FilterConfig} 是 Servlet 类型、且当前无使用点，两侧适配器沿用 Servlet 接口的默认实现（空）。</li>
 *   <li><b>不再需要 Servlet 版到 HTTP 版的桥接</b>：原 {@code ICFilter} 要把
 *   {@code doFilter(ServletRequest, ServletResponse, FilterChain)} 强转成 HTTP 版本；本接口直接以
 *   {@link CHttpRequest} / {@link CHttpResponse} 为参数，强转只在适配器里做一次。</li>
 *   <li><b>异常</b>：底层 {@code ServletException} 由适配器包装为 {@link CServletException}（运行时异常），
 *   故本接口只声明 {@link IOException}。</li>
 *   <li><b>注册仍走容器</b>：给 Spring 用时把 {@code CFilterAdapter.of(filter)} 注册为 Filter Bean
 *   （或包进 FilterRegistrationBean），优先级等容器语义仍由容器侧决定。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>无兜底：本接口只声明契约；是否放行、失败如何处理由实现决定，异常原样向上传播（底层 Servlet 异常已包装）。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要同时兼容 javax 与 jakarta 两套容器的过滤器实现，或需要在两套容器间可切换的公共过滤器代码。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要按 {@code FilterConfig} 读取初始化参数、或需要在 {@code init}/{@code destroy} 做资源管理的场景：
 *   本接口不暴露这两个回调，需直接实现对应容器的 {@code Filter}。</li>
 *   <li>需要异步过滤器（Servlet 3.0 {@code asyncSupported}）等容器专有能力的场景不适用。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>与接口配套的 {@link CFilterChain} 是<b>一次性</b>对象：一次请求的链只能调用一次放行，
 *   与 Servlet 规范一致，本接口不做重放保护。</li>
 *   <li>适配器要求传入的请求/响应来自<b>同侧</b>适配器（{@code CHttpServletRequest}/{@code CHttpServletResponse}），
 *   否则解包失败——抽象层对象不跨 javax/jakarta 混用。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.0
 */
public interface CFilter {

    /**
     * 过滤请求：处理完可调用 {@link CFilterChain#doFilter} 放行，也可直接结束本次请求
     *
     * @param request  请求
     * @param response 响应
     * @param chain    过滤器链
     * @throws IOException 读写请求或响应失败时
     */
    void doFilter(CHttpRequest request, CHttpResponse response, CFilterChain chain) throws IOException;

}
