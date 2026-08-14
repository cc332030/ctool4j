package com.c332030.ctool4j.log.interceptor;

import cn.hutool.core.util.BooleanUtil;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.config.CRequestLogConfig;
import com.c332030.ctool4j.web.interceptor.ICHandlerInterceptor;
import com.c332030.ctool4j.web.util.CRequestLogUtils;
import com.c332030.ctool4j.web.util.CTraceUtils;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.val;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

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
    public void afterCompletion(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        @Nullable Exception ex
    ) {
        try {

            val requestLogOpt = CRequestLogUtils.getOptThenRemove();
            requestLogOpt.ifPresent(requestLog -> {
                // 慢日志开关关闭时直接跳过，不做耗时计算，避免浪费 CPU
                if (!BooleanUtil.isTrue(config.getSlowLogEnable())) {
                    return;
                }
                // 完整耗时：endTimeMillis 由 beforeBodyWrite 设置（早于响应体写出），
                // 此处以实际完成时间为准，包含视图渲染与响应写出耗时；
                // 未记录开始时间时耗时恒为 0（getRt 在 beginTimeMillis 未设置时返回 null，兜底即 0）
                long rt;
                if (requestLog.getBeginTimeMillis() > 0) {
                    rt = System.currentTimeMillis() - requestLog.getBeginTimeMillis();
                } else {
                    rt = 0L;
                }
                if (rt > config.getSlowLogMillis()) {
                    log.warn("slow request, url: {}, cost: {}", CRequestUtils.getRequestURIDefaultNull(), rt);
                }
            });

            CTraceUtils.removeTraceInfo();
        } catch (Throwable e) {
            log.error("removeTraceInfo failure", e);
        }
    }

}
