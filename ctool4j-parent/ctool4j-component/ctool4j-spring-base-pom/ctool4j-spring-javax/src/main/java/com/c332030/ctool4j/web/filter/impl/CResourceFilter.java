package com.c332030.ctool4j.web.filter.impl;

import com.c332030.ctool4j.web.constant.CResourceUrlConstants;
import com.c332030.ctool4j.web.filter.ICFilter;
import lombok.CustomLog;
import lombok.val;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 * Description: CResourceFilter
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CResourceFilter} 为静态资源忽略过滤器，{@code @Component} + {@code ICFilter} + {@code PriorityOrdered}， {@code getOrder()} 返回 {@code Integer.MIN_VALUE}（最高优先级，最先执行）。</p>
 * <p>核心方法 {@code doFilter(request, response, chain)}：</p>
 * <ul>
 *   <li>取 {@code request.getRequestURI()}</li>
 *   <li>命中 {@code CResourceUrlConstants.IGNORE_RESOURCE_URLS} 时，记录 debug 日志、以 204 状态码结束请求并返回</li>
 *   <li>否则 {@code chain.doFilter} 继续请求链</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>uri 不在忽略集合</td>
 *     <td>放行到后续过滤器链</td>
 *   </tr>
 *   <tr>
 *     <td>命中忽略集合</td>
 *     <td>记录 debug 日志，返回 204</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要忽略 favicon 等静态资源请求的 web 应用。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>忽略资源集合来自 {@code CResourceUrlConstants} 常量，新增忽略资源需改常量。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>忽略静态资源</b></p>
 * <ul>
 *   <li>对 favicon 等无需处理的静态资源直接以 204 结束，避免进入业务处理。</li>
 * </ul>
 *
 * @since 2026/1/28
 * @version 1.0
 */
@CustomLog
@Component
public class CResourceFilter implements ICFilter, PriorityOrdered {

    /**
     * 最高优先级，最先执行
     *
     * @return 优先级
     */
    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    /**
     * 过滤请求：命中忽略资源 URL 时直接返回 204
     *
     * @param request  请求
     * @param response 响应
     * @param chain    过滤器链
     */
    @Override
    public void doFilter(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain
    ) throws ServletException, IOException {

        val requestURI = request.getRequestURI();
        if(CResourceUrlConstants.IGNORE_RESOURCE_URLS.contains(requestURI)) {
            log.debug("ignore: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        chain.doFilter(request, response);

    }

}
