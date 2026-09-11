package com.c332030.ctool4j.feign.interceptor;

import com.c332030.ctool4j.feign.util.CFeignUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.AllArgsConstructor;
import lombok.CustomLog;

/**
 * <p>
 * Description: CFeignInterceptor
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CFeignInterceptor} 实现 feign {@code RequestInterceptor}，拦截请求：命中拦截规则时跳过，否则透传请求头。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>委托 {@code CFeignUtils} 完成拦截判断与请求头透传。</li>
 *   <li>拦截/透传异常记录 error 日志，不向上抛出（避免影响请求执行）。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>拦截处理异常时记录日志，不中断请求（透传失败则请求不携带额外 header，但主流程继续）。</li>
 * </ul>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>Feign 全局请求拦截与 header 透传。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>异常静默（仅日志），若透传 header 是业务必需，失败可能影响下游，调用方需注意。</li>
 * </ul>
 *
 * @since 2025/9/21
 * @version 1.0
 */
@CustomLog
@AllArgsConstructor
public class CFeignInterceptor implements RequestInterceptor {

    /**
     * 拦截请求：命中拦截规则时跳过，否则透传请求头
     * <ul>
     *   <li>{@code apply(template)}：命中 {@code CFeignUtils.intercept} 则跳过；否则 {@code CFeignUtils.transferHeaders} 透传请求头。</li>
     * </ul>
     *
     * @param template 请求模板
     */
    @Override
    public void apply(RequestTemplate template) {
        try {

            if(CFeignUtils.intercept(template)) {
                return;
            }
            CFeignUtils.transferHeaders(template);
        } catch (Throwable t) {
            log.error("transferHeaders error", t);
        }
    }

}
