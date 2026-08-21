package com.c332030.ctool4j.web.cors.filter;

import com.c332030.ctool4j.web.cors.util.CCorsUtils;
import com.c332030.ctool4j.web.filter.ICFilter;
import lombok.CustomLog;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 * Description: CCorsFilter
 * </p>
 *
 * @since 2026/1/10
 * @see doc/design/web/CCorsFilter.adoc
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

        CCorsUtils.handle(request, response);
        if(CCorsUtils.handleOptions(request, response)) {
            return;
        }

        chain.doFilter(request, response);

    }

}
