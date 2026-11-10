package com.c332030.ctool4j.web.filter.impl;

import com.c332030.ctool4j.model.CHttpServletRequest;
import com.c332030.ctool4j.model.CHttpServletResponse;
import com.c332030.ctool4j.web.filter.ICFilter;
import com.c332030.ctool4j.web.util.CResourceUrlUtils;
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
 *   <li>包装为抽象层对象后调用 {@code CResourceUrlUtils.handleAndContinue(request, response)}：
 *   命中忽略集合时记 debug 日志并以 204 结束（返回 false）</li>
 *   <li>{@code false} 时直接返回；否则 {@code chain.doFilter} 继续请求链</li>
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
 *     <td>由 {@code CResourceUrlUtils} 记 debug 日志并置 204，本过滤器直接返回</td>
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
 * <p><b>只留容器相关部分</b></p>
 * <ul>
 *   <li>本类只做包装与放行判定；忽略集合判定与 204 结束收在 base 的 {@code CResourceUrlUtils}，
 *   两侧不重复实现（见 {@code agent/AGENTS-PROJECT.MD} 的双栈规范：抽象层 + 两侧同名适配）。</li>
 * </ul>
 *
 * @since 2026/1/28
 * @version 1.1
 */
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

        if(!CResourceUrlUtils.handleAndContinue(CHttpServletRequest.of(request), CHttpServletResponse.of(response))) {
            return;
        }

        chain.doFilter(request, response);

    }

}
