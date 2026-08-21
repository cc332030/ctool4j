package com.c332030.ctool4j.web.filter.impl;

import com.c332030.ctool4j.web.constant.ResourceUrlConstants;
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
 * @since 2026/1/28
 * @see "doc/design/web/CResourceFilter.adoc"
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
        if(ResourceUrlConstants.IGNORE_RESOURCE_URLS.contains(requestURI)) {
            log.debug("ignore: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        chain.doFilter(request, response);

    }

}
