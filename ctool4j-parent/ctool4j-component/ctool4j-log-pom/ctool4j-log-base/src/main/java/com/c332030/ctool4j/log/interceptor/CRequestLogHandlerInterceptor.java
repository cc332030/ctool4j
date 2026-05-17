package com.c332030.ctool4j.log.interceptor;

import cn.hutool.core.util.BooleanUtil;
import com.c332030.ctool4j.log.config.CRequestLogConfig;
import com.c332030.ctool4j.log.util.CRequestLogUtils;
import com.c332030.ctool4j.log.util.CTraceUtils;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.interceptor.ICHandlerInterceptor;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.val;
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
@AllArgsConstructor
public class CRequestLogHandlerInterceptor implements ICHandlerInterceptor {

    CRequestLogConfig config;

    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) {
        try {
            CTraceUtils.initTrace();
            CRequestLogUtils.init();
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
    ) {
        try {

            val requestLogOpt = CRequestLogUtils.getOptThenRemove();
            requestLogOpt.ifPresent(requestLog -> {
                val rt = requestLog.getRt();
                if (BooleanUtil.isTrue(config.getSlowLogEnable())
                    && rt > config.getSlowLogMillis()) {
                    log.warn("slow request, url: {}, cost: {}", CRequestUtils.getRequestURIDefaultNull(), rt);
                }
            });

            CTraceUtils.removeTraceInfo();
        } catch (Throwable e) {
            log.error("removeTraceInfo failure", e);
        }
    }

}
