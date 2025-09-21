package com.c332030.ctool.feign.interceptor;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import com.c332030.ctool.core.util.CCommUtils;
import com.c332030.ctool.feign.config.CFeignLogConfig;
import com.c332030.ctool.feign.util.CFeignUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.val;

import java.nio.charset.StandardCharsets;

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

    CFeignLogConfig feignLogConfig;

    @Override
    public void apply(RequestTemplate template) {

        if(!BooleanUtil.isTrue(feignLogConfig.getEnable())) {
            return;
        }

        try {
            dealLog(template);
        } catch (Throwable e) {
            log.error("处理请求日志失败", e);
        }

    }

    private void dealLog(RequestTemplate template) {

        val method = template.method();
        val url = template.url();

        val httpLog = new StringBuilder();
        CFeignUtils.HTTP_LOG_THREAD_LOCAL.set(httpLog);

        httpLog.append("\n");
        httpLog.append(method);
        httpLog.append(" ");
        httpLog.append(url);

        val headers = template.headers();
        if(BooleanUtil.isTrue(feignLogConfig.getEnableHeader())) {
            httpLog.append("\n\n");
            httpLog.append(CCommUtils.getFullHeaderStr(headers));
        }

        httpLog.append("\n\n");
        val bodyBytes = template.body();
        if(ArrayUtil.isEmpty(bodyBytes)) {

            httpLog.append("[no request body]");
        } else {

            val requestBody = new String(bodyBytes, StandardCharsets.UTF_8);
            if(CCommUtils.isTextBody(headers)) {
                httpLog.append(requestBody);
            } else {
                httpLog.append("[no request text body]");
            }

        }

    }

}
