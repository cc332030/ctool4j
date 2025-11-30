package com.c332030.ctool4j.job.xxljob.aspect;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.job.xxljob.config.CXxlJobExecutorLogConfig;
import com.c332030.ctool4j.job.xxljob.util.CXxlJobUtils;
import com.c332030.ctool4j.spring.util.CAspectUtils;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: CXxlJobAspect
 * </p>
 *
 * @since 2025/11/28
 */
@CustomLog
@Aspect
@Component
@AllArgsConstructor
public class CXxlJobAspect {

    CXxlJobExecutorLogConfig executorConfig;

    /**
     * 拦截 @XxlJob，切入点
     */
    @Pointcut("@annotation(com.xxl.job.core.handler.annotation.XxlJob)")
    public void annotationPointcut(){}

    /**
     * 拦截 @XxlJob
     * @param joinPoint 切入点
     * @return 方法执行结果
     */
    @SneakyThrows
    @Around("annotationPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) {

        val method = CAspectUtils.getMethod(joinPoint);
        val jobName = CReflectUtils.getAnnotationValue(method, XxlJob.class, XxlJob::value);

        val args = joinPoint.getArgs();
        log.debug("jobName: {}, args: {}", jobName, args);

        if(ArrayUtil.isNotEmpty(args)) {

            val arg0 = args[0];

            val parameterTypes = method.getParameterTypes();
            if(String.class == parameterTypes[0] && StrUtil.isBlank((String)arg0)) {

                val jobParam = CXxlJobUtils.getJobParam();
                if(StrUtil.isNotBlank(jobParam)) {
                    log.info("jobName: {}, jobParam: {}", jobParam);
                    args[0] = jobParam;
                }
            }
        }

        try {
            return joinPoint.proceed(args);
        } catch (Throwable e) {
            if(BooleanUtil.isTrue(executorConfig.getLogCatchError())) {
                log.error("jobName: {} failure", jobName, e);
            }
            throw e;
        }
    }

}
