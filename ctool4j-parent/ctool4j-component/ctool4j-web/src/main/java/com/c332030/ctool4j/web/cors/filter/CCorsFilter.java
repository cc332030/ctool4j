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
 */
@CustomLog
@Component
public class CCorsFilter implements ICFilter, PriorityOrdered {

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        try {

            CCorsUtils.handle(request, response);
            if(CCorsUtils.handleOptions(request, response)) {
                return;
            }
        } catch (Throwable e) {
            log.error("deal cors failure", e);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            CCorsUtils.clear();
        }

    }

}
