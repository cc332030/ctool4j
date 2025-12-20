package com.c332030.ctool4j.log.interceptor;

import com.c332030.ctool4j.log.util.CTraceUtils;
import com.c332030.ctool4j.web.interceptor.ICHandlerInterceptor;
import lombok.CustomLog;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Description: CRequestLogHandlerInterceptor
 * </p>
 *
 * @since 2025/9/28
 */
@CustomLog
@Component
public class CRequestLogHandlerInterceptor implements ICHandlerInterceptor {

    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) throws Exception {
        try {
            CTraceUtils.initTrace();
        } catch (Throwable e) {
            log.error("initTrace failure", e);
        }
        return true;
    }

    @Override
    public void postHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        @Nullable ModelAndView modelAndView
    ) throws Exception {
        try {
            CTraceUtils.removeTraceInfo();
        } catch (Throwable e) {
            log.error("removeTraceInfo failure", e);
        }
    }

}
