package com.c332030.ctool4j.log.aspect;

import cn.hutool.core.util.BooleanUtil;
import com.c332030.ctool4j.core.log.CLogUtils;
import com.c332030.ctool4j.log.config.CRequestLogConfig;
import com.c332030.ctool4j.log.util.CRequestLogUtils;
import com.c332030.ctool4j.spring.util.CAspectUtils;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import lombok.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * <p>
 * Description: RequestLogConfiguration
 * </p>
 *
 * @author c332030
 * @since 2024/5/6
 */
@CustomLog
@Aspect
@Component
@AllArgsConstructor
public class CRequestLogAspect {

    CRequestLogConfig requestLogConfig;

    @Pointcut(
        "@annotation(org.springframework.web.bind.annotation.RequestMapping) "
            + "|| @annotation(org.springframework.web.bind.annotation.GetMapping) "
            + "|| @annotation(org.springframework.web.bind.annotation.PostMapping) "
            + "|| @annotation(org.springframework.web.bind.annotation.PutMapping) "
            + "|| @annotation(org.springframework.web.bind.annotation.PatchMapping) "
            + "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping)"
    )
    public void annotationPointcut() {}

    @SneakyThrows
    @Around("annotationPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) {

        Object result = null;
        Throwable throwable = null;
        val args = joinPoint.getArgs();

        val startMills = System.currentTimeMillis();
        var costMills = 0L;

        val isLogEnable = CRequestLogUtils.isAopEnable();
        var hasRequest = false;
        try {

            if (isLogEnable && (hasRequest = CRequestUtils.hasRequest())) {

                val method = CAspectUtils.getMethod(joinPoint);
                val parameters = method.getParameters();

                val argMap = new LinkedHashMap<String, Object>(parameters.length);
                for (int i = 0; i < parameters.length; i++) {
                    val parameter = parameters[i];
                    argMap.put(parameter.getName(), dealArg(args[i]));
                }

                CRequestLogUtils.init(argMap);

            }
        } catch (Throwable e) {
            log.error("init request log failure", e);
        }

        try {

            result = joinPoint.proceed(args);
            return result;
        } catch (Throwable e) {
            throwable = e;
            throw e;
        } finally {

            costMills = System.currentTimeMillis() - startMills;
            if (BooleanUtil.isTrue(requestLogConfig.getSlowLogEnable())
                && costMills > requestLogConfig.getSlowLogMillis()) {
                log.warn("slow request, url: {}, cost: {}", CRequestUtils.getRequestURIDefaultNull(), costMills);
            }

            try {
                if (isLogEnable && hasRequest) {
                    CRequestLogUtils.write(result, throwable);
                }
            } catch (Throwable e) {
                log.error("write request log failure", e);
            }
        }

    }

    private Object dealArg(Object arg) {

        if (null == arg) {
            return null;
        }

        if (arg instanceof Serializable) {
            return arg;
        }

        val argClass = arg.getClass();
        if (CLogUtils.isJsonLog(argClass)) {
            return arg;
        }

        return argClass.getName();
    }

}
