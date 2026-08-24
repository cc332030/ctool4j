package com.c332030.ctool4j.log.interceptor;

import com.c332030.ctool4j.web.config.CRequestLogConfig;
import com.c332030.ctool4j.web.interceptor.ICHandlerInterceptor;
import com.c332030.ctool4j.web.util.CCommUtils;
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
 * @see "doc/design/log/CRequestLogHandlerInterceptor.adoc"
 * @since 2025/9/28
 */
@CustomLog
@Component
@AllArgsConstructor
public class CRequestLogHandlerInterceptor implements ICHandlerInterceptor {

    CRequestLogConfig config;

    /**
     * 请求前处理：初始化链路追踪与请求日志上下文
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @return 是否继续处理，恒为 true
     */
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

    /**
     * 请求完成后处理：打印请求日志、输出慢日志并清理链路追踪上下文
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @param ex       处理异常
     */
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

                // 请求日志打印（受 enable 总开关控制）：endTimeMillis 以实际完成时间为准，
                // 覆盖 beforeBodyWrite 记录响应体时的时间，包含视图渲染与响应写出耗时
                if (CRequestLogUtils.isEnable()) {
                    requestLog.setEndTimeMillis(System.currentTimeMillis());
                    CRequestLogUtils.logWrite(requestLog, config.getEnableHeader());
                }

                // 慢日志：不受 enable 总开关控制，由 slowLogEnable 独立控制（默认启用）；
                // endTimeMillis 以实际完成时间为准，供 logSlowRequest 计算耗时（不受 enable 影响，保证有值）
                requestLog.setEndTimeMillis(System.currentTimeMillis());
                CCommUtils.logSlowRequest(config, requestLog);
            });

            // 先清 MDC 再清 ThreadLocal：removeTraceId 内部依赖当前 ThreadLocal 的 traceInfo，
            // 若先 removeTraceInfo 会触发 withInitial 重建实例导致残留
            CTraceUtils.removeTraceId();
            CTraceUtils.removeTraceInfo();
        } catch (Throwable e) {
            log.error("removeTraceInfo failure", e);
        }
    }

}
