package com.c332030.ctool4j.web.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 * Description: ICFilter
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICFilter} 为项目统一的 HTTP Servlet 过滤器接口，继承 {@code Filter}， 将 {@code doFilter(ServletRequest, ServletResponse, FilterChain)} 委托给 HTTP 版本 {@code doFilter(HttpServletRequest, HttpServletResponse, FilterChain)}。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>默认方法强制转换</td>
 *     <td>将 ServletRequest/Response 强转为 HTTP 版本</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>项目内所有 Servlet 过滤器实现该接口。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>实现类需实现 HTTP 版本的 {@code doFilter}。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>统一入口</b></p>
 * <ul>
 *   <li>定义 HTTP 版本的 {@code doFilter}，实现类只需处理 HTTP 请求/响应，无需处理 ServletRequest 强转。</li>
 * </ul>
 *
 * @since 2025/9/25
 * @version 1.0
 */
public interface ICFilter extends Filter {

    /**
     * 过滤请求（委托给 HTTP 版本）
     * @param request 请求
     * @param response 响应
     * @param chain 过滤器链
     * @throws IOException IO 异常
     * @throws ServletException Servlet 异常
     */
    @Override
    default void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    /**
     * 过滤 HTTP 请求
     * @param request 请求
     * @param response 响应
     * @param chain 过滤器链
     * @throws IOException IO 异常
     * @throws ServletException Servlet 异常
     */
    void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException;

}
