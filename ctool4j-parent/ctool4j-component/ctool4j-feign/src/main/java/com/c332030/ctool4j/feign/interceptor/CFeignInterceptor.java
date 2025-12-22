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
 * @since 2025/9/21
 */
@CustomLog
@AllArgsConstructor
public class CFeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        try {

            if(CFeignUtils.intercept(template)) {
                return;
            }
            CFeignUtils.dealHeaders(template);
        } catch (Throwable t) {
            log.error("dealHeaders error", t);
        }
    }

}
