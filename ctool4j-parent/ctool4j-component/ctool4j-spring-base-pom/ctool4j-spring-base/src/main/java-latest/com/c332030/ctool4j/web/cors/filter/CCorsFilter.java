package com.c332030.ctool4j.web.cors.filter;

import com.c332030.ctool4j.model.CHttpServletRequest;
import com.c332030.ctool4j.model.CHttpServletResponse;
import com.c332030.ctool4j.web.cors.util.CCorsUtils;
import com.c332030.ctool4j.web.filter.ICFilter;
import lombok.CustomLog;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 * Description: CCorsFilter
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CCorsFilter} 为跨域（CORS）Servlet Filter，{@code @Component} + {@code ICFilter} + {@code PriorityOrdered}， {@code getOrder()} 返回 {@code Integer.MIN_VALUE}（最高优先级，最先执行）。</p>
 * <p>核心方法 {@code doFilter(request, response, chain)}：</p>
 * <ul>
 *   <li>包装为抽象层对象后调用 {@code CCorsUtils.handleAndContinue(request, response)}：输出 CORS 头，并判定是否继续</li>
 *   <li>{@code false}（OPTIONS 预检已由 204 结束）时直接 return；否则 {@code chain.doFilter} 继续请求链</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>{@code CCorsUtils.handle}/{@code handleOptions} 内部异常</td>
 *     <td>被 CCorsUtils 内部 try-catch 记录日志，请求继续</td>
 *   </tr>
 *   <tr>
 *     <td>非预检请求</td>
 *     <td>正常放行到过滤器链后续</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>跨域请求自动输出 CORS 头并正确处理 OPTIONS 预检。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>{@code CCorsConfig.enable=false} 时 {@code CCorsUtils.handle} 不做任何输出（开关控制）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>依赖 {@code CCorsUtils} 的配置注入（Spring 容器初始化后生效）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>最高优先级</b></p>
 * <ul>
 *   <li>最先执行，保证跨域头在请求链最前面输出。</li>
 * </ul>
 * <p><b>预检处理</b></p>
 * <ul>
 *   <li>OPTIONS 预检请求由 {@code CCorsUtils.handleOptions} 以 204 结束，不进入后续业务处理。</li>
 * </ul>
 *
 * @since 2026/1/10
 * @version 1.1
 */
@CustomLog
@Component
public class CCorsFilter implements ICFilter, PriorityOrdered {

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
     * 过滤请求：输出 CORS 头，OPTIONS 预检请求直接返回
     *
     * @param request  请求
     * @param response 响应
     * @param chain    过滤器链
     */
    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        // 只做包装与放行判定：跨域处理与预检编排收在 base 的 CCorsUtils，两侧不重复实现
        if(!CCorsUtils.handleAndContinue(CHttpServletRequest.of(request), CHttpServletResponse.of(response))) {
            return;
        }

        chain.doFilter(request, response);

    }

}
