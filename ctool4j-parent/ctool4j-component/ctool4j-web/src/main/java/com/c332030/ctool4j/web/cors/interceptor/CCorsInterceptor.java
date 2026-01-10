package com.c332030.ctool4j.web.cors.interceptor;

import com.c332030.ctool4j.web.cors.util.CCorsUtils;
import com.c332030.ctool4j.web.interceptor.ICHandlerInterceptor;
import lombok.CustomLog;
import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Description: CCorsInterceptor
 * </p>
 *
 * @since 2025/9/28
 */
@CustomLog
//@Component
public class CCorsInterceptor implements ICHandlerInterceptor {

    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) {

        try {

            CCorsUtils.handle(request, response);

            if(HttpMethod.OPTIONS.name().equalsIgnoreCase(request.getMethod())) {
                log.debug("deal OPTIONS request");
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return false;
            }
        } catch (Throwable e) {
            log.error("deal cors failure", e);
        }

        return false;
    }

}
