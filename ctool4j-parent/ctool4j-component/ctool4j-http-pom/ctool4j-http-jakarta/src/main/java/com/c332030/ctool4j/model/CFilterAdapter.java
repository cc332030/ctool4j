package com.c332030.ctool4j.model;

import com.c332030.ctool4j.interfaces.CFilter;
import com.c332030.ctool4j.interfaces.CFilterChain;
import com.c332030.ctool4j.interfaces.CHttpRequest;
import com.c332030.ctool4j.interfaces.CHttpResponse;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.IOException;
import java.util.Objects;

/**
 * <p>
 * Description: CFilterAdapter
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CFilterAdapter} 是 {@link CFilter} 在 <b>jakarta</b> 侧的落地（适配器）：
 * 实现 {@code jakarta.servlet.Filter}，把容器的 Servlet 请求/响应与过滤器链转换成本抽象层的类型后，
 * 委托给被包装的 {@link CFilter}。</p>
 * <p>创建入口：{@link #of(CFilter)}。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>转换只在这里做一次</b>：{@code ServletRequest}/{@code ServletResponse} 到
 *   {@link CHttpRequest}/{@link CHttpResponse} 的强转、{@code FilterChain} 的包装都在本类完成，
 *   过滤器实现（{@link CFilter}）不再接触 Servlet 类型。</li>
 *   <li><b>生命周期回调不转发</b>：{@code init(FilterConfig)} 与 {@code destroy()} 沿用 Servlet 接口的默认实现（空）；
 *   需要初始化参数时直接实现容器的 {@code Filter}，不经本适配器。</li>
 *   <li><b>注册方式</b>：Spring 下把 {@code CFilterAdapter.of(filter)} 作为 Filter Bean（或包进
 *   FilterRegistrationBean）注册，优先级等容器语义仍由容器侧决定。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>创建时校验被包装的过滤器非空（{@link #of(CFilter)} 的 {@code null} 入参快速失败）。</li>
 *   <li>不做其它兜底：请求/响应强转失败按 JVM 语义抛 {@code ClassCastException}（容器传入的本就是 HTTP 请求/响应）。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>运行在 jakarta（Spring Boot 3.x / Servlet 5.0+）容器下、需要注册 {@link CFilter} 实现为过滤器的场景。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>一次请求只应调用一次 {@link CFilterChain#doFilter}，与 Servlet 规范一致；本类不做重复调用保护。</li>
 *   <li>被包装的 {@link CFilter} 抛出的 {@code CServletException}（运行时）会直接穿透 Servlet 容器——
 *   容器按未受检异常处理（通常返回 500），需要转换时由过滤器实现自行处理。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.0
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CFilterAdapter implements Filter {

    private final CFilter filter;

    /**
     * 以抽象层过滤器创建适配器
     *
     * @param filter 抽象层过滤器，不可为 null
     * @return 适配器实例
     * @throws NullPointerException 过滤器为 null 时
     */
    public static CFilterAdapter of(CFilter filter) {
        Objects.requireNonNull(filter, "filter");
        return new CFilterAdapter(filter);
    }

    /**
     * 把容器的 Servlet 请求/响应与过滤器链转换为抽象层类型后委托给被包装的过滤器
     *
     * @param request  Servlet 请求
     * @param response Servlet 响应
     * @param chain    Servlet 过滤器链
     * @throws IOException      读写请求或响应失败时
     * @throws ServletException 沿用 Servlet 接口声明；本实现不主动抛出（底层异常已包装为运行时异常）
     */
    @Override
    public void doFilter(
        ServletRequest request,
        ServletResponse response,
        FilterChain chain
    ) throws IOException, ServletException {

        val httpRequest = CHttpServletRequest.of((HttpServletRequest) request);
        val httpResponse = CHttpServletResponse.of((HttpServletResponse) response);
        val httpChain = CFilterChainAdapter.of(chain);

        filter.doFilter(httpRequest, httpResponse, httpChain);

    }

}
