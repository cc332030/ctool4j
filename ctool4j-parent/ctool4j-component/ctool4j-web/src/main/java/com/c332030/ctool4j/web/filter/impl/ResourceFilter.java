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
 * Description: ResourceFilter
 * </p>
 *
 * @since 2026/1/28
 */
@CustomLog
@Component
public class ResourceFilter implements ICFilter, PriorityOrdered {

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

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
