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
 * @see "doc/design/feign/CFeignInterceptor.adoc"
 * @since 2025/9/21
 */
@CustomLog
@AllArgsConstructor
public class CFeignInterceptor implements RequestInterceptor {

    /**
     * 拦截请求：命中拦截规则时跳过，否则透传请求头
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
