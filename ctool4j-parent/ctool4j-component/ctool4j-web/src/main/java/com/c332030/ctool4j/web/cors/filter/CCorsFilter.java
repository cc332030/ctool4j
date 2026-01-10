package com.c332030.ctool4j.web.cors.filter;

import com.c332030.ctool4j.web.cors.util.CCorsUtils;
import com.c332030.ctool4j.web.filter.ICFilter;
import lombok.CustomLog;
import org.springframework.core.PriorityOrdered;
import org.springframework.http.HttpMethod;
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

            if(HttpMethod.OPTIONS.name().equalsIgnoreCase(request.getMethod())) {
                log.debug("deal OPTIONS request");
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }
        } catch (Throwable e) {
            log.error("deal cors failure", e);
        }

        chain.doFilter(request, response);

    }

}
