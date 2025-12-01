package com.c332030.ctool4j.feign.aspect;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import com.c332030.ctool4j.core.util.CCommUtils;
import com.c332030.ctool4j.core.util.CThreadLocalUtils;
import com.c332030.ctool4j.feign.config.CFeignLogConfig;
import com.c332030.ctool4j.feign.util.CFeignUtils;
import feign.Request;
import feign.Response;
import feign.Util;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

/**
 * <p>
 * Description: CFeignClientAspect
 * </p>
 *
 * @since 2025/12/1
 */
@CustomLog
@Aspect
@Component
@AllArgsConstructor
public class CFeignClientAspect {

    CFeignLogConfig feignLogConfig;

    /**
     * 切入点
     */
    @Pointcut("execution(* feign.Client.execute(..))")
    public void annotationPointcut(){}

    /**
     * 拦截
     * @param joinPoint 切入点
     * @return 方法执行结果
     */
    @SneakyThrows
    @Around("annotationPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) {

        val args = joinPoint.getArgs();
        val request = (Request)args[0];
        val response = (Response)joinPoint.proceed(args);

        if(BooleanUtil.isTrue(feignLogConfig.getEnable())) {
            try {
                return dealResponse(request, response);
            } catch (Throwable e) {
                log.error("处理响应日志失败", e);
            } finally {
                response.close();
            }
        }

        return response;
    }

    @SneakyThrows
    private Response dealResponse(Request request, Response response) {

        val headers = response.headers();
        val bodyBytes = Util.toByteArray(response.body().asInputStream());

        try {
            dealLog(headers, bodyBytes);
        } catch (Throwable e) {
            log.error("处理响应日志失败", e);
        }

        // 重新构建响应体（因为原流已被读取）
        return Response.builder()
                .requestTemplate(request.requestTemplate())
//                .protocolVersion(response.protocolVersion()) // 低版本不支持
                .status(response.status())
                .reason(response.reason())
                .request(request)
                .headers(headers)
                .body(bodyBytes)
                .build();
    }

    private void dealLog(Map<String, Collection<String>> headers, byte[] bodyBytes) {

        val httpLog = CThreadLocalUtils.getThenRemove(CFeignUtils.HTTP_LOG_THREAD_LOCAL);

        if(BooleanUtil.isTrue(feignLogConfig.getEnableHeader())) {
            httpLog.append("\n\n");
            httpLog.append(CCommUtils.getFullHeaderStr(headers));
        }

        httpLog.append("\n\n");
        if(ArrayUtil.isEmpty(bodyBytes)) {

            httpLog.append("[no response body]");
        } else {

            if(CCommUtils.isTextBody(headers)) {

                val responseBody = new String(bodyBytes, StandardCharsets.UTF_8);
                httpLog.append(responseBody);
            } else {
                httpLog.append("[no response text body]");
            }

        }

    }


}
