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
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code preHandle}：初始化链路追踪与请求日志上下文（{@code CTraceUtils.initTrace}、{@code CRequestLogUtils.init}）。</li>
 *   <li>{@code afterCompletion}：打印请求日志、输出慢日志、清理链路追踪上下文。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>preHandle 初始化异常</td>
 *     <td>捕获记录 error 日志，继续处理</td>
 *   </tr>
 *   <tr>
 *     <td>请求日志未启用</td>
 *     <td>跳过日志打印（慢日志仍判断，不受 enable 控制）</td>
 *   </tr>
 *   <tr>
 *     <td>慢日志开关关闭</td>
 *     <td>{@code CCommUtils.logSlowRequest} 直接跳过，不做耗时计算</td>
 *   </tr>
 *   <tr>
 *     <td>未记录开始时间</td>
 *     <td>耗时恒为 0</td>
 *   </tr>
 *   <tr>
 *     <td>afterCompletion 清理异常</td>
 *     <td>捕获记录 error 日志</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>Web 请求的访问日志采集、链路追踪清理。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>依赖 {@code ICHandlerInterceptor} 调用链与请求日志配置（web 模块）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>清理顺序依赖（先 MDC 后 ThreadLocal）有强约束，改动需谨慎。</li>
 *   <li>慢日志耗时计算以实际完成时间为准，包含视图渲染与响应写出耗时。</li>
 * </ul>
 *
 * @since 2025/9/28
 * @version 1.0
 */
@CustomLog
@Component
@AllArgsConstructor
public class CRequestLogHandlerInterceptor implements ICHandlerInterceptor {

    CRequestLogConfig config;

    /**
     * 请求前处理：初始化链路追踪与请求日志上下文
     *
     * <h2>preHandle</h2>
     * <ul>
     *   <li>调用 {@code CTraceUtils.initTrace()} 与 {@code CRequestLogUtils.init()}。</li>
     *   <li>异常捕获后记录 error 日志，恒返回 true 继续处理。</li>
     * </ul>
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @return 是否继续处理，恒为 true*/
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
     * <h2>afterCompletion</h2>
     * <ul>
     *   <li>获取并移除请求日志（{@code getOptThenRemove}），存在则：</li>
     *   <li>受 enable 总开关控制，{@code setEndTimeMillis(当前时间)} 后 {@code logWrite} 打印请求日志（覆盖响应体采集时记录的时间，含视图渲染与响应写出耗时）。</li>
     *   <li>慢日志：不受 enable 控制，由 {@code slowLogEnable}/{@code slowLogMillis}（继承自 {@code CRequestLogBaseConfig}）独立控制，耗时以实际完成时间为准，统一走 {@code CCommUtils.logSlowRequest}；未记录开始时间时耗时恒为 0。</li>
     *   <li>先清 MDC（{@code removeTraceId}）再清 ThreadLocal（{@code removeTraceInfo}）——removeTraceId 内部依赖当前 ThreadLocal 的 traceInfo。</li>
     * </ul>
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @param ex       处理异常*/
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
