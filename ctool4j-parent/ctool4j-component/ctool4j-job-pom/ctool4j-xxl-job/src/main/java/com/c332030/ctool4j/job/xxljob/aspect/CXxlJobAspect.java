package com.c332030.ctool4j.job.xxljob.aspect;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.job.xxljob.config.CXxlJobExecutorLogConfig;
import com.c332030.ctool4j.job.xxljob.util.CXxlJobUtils;
import com.c332030.ctool4j.spring.util.CAspectUtils;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.*;
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
 * <h2>能力目录</h2>
 * <p>{@code CXxlJobAspect}（{@code @Aspect}）拦截标注 {@code @XxlJob} 的方法：</p>
 * <ul>
 *   <li>参数为空且方法首参为 String 时，用 {@code CXxlJobUtils.getJobParam()} 填充（jobParam 非空时）。</li>
 *   <li>可按配置打印执行耗时（logCost）与捕获错误（logCatchError）。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>首参为空但 jobParam 为空</td>
 *     <td>不填充</td>
 *   </tr>
 *   <tr>
 *     <td>异常</td>
 *     <td>记录后继续抛出</td>
 *   </tr>
 *   <tr>
 *     <td>logCost 关闭</td>
 *     <td>不打印耗时</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>xxl-job 任务的统一拦截、参数注入与耗时日志。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>仅对首参为 String 的方法注入参数。</li>
 *   <li>依赖 {@code CXxlJobExecutorLogConfig} 配置。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>参数填充</b></p>
 * <ul>
 *   <li>当首参类型为 String 且为空时，用 xxl-job 的任务参数填充。</li>
 * </ul>
 * <p><b>环绕逻辑</b></p>
 * <ul>
 *   <li>logCost 开启时记录开始时间，finally 打印耗时。</li>
 *   <li>logCatchError 开启时 catch 记录错误并继续抛出。</li>
 * </ul>
 *
 * @since 2025/11/28
 * @version 1.0
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
        val jobName = CReflectUtils.getAnnotationValueCached(method, XxlJob.class, XxlJob::value);

        val args = joinPoint.getArgs();
        log.debug("jobName: {}, args: {}", jobName, args);

        if(ArrayUtil.isNotEmpty(args)) {

            val arg0 = args[0];

            val parameterTypes = method.getParameterTypes();
            if(String.class == parameterTypes[0] && StrUtil.isBlank((String)arg0)) {

                val jobParam = CXxlJobUtils.getJobParam();
                if(StrUtil.isNotBlank(jobParam)) {
                    log.info("jobName: {}, jobParam: {}", jobName, jobParam);
                    args[0] = jobParam;
                }
            }
        }

        var start = 0L;
        if(BooleanUtil.isTrue(executorConfig.getLogCost())) {
            start = System.currentTimeMillis();
            log.info("jobName start: {}", jobName);
        }

        try {
            return CAspectUtils.process(joinPoint);
        } catch (Throwable e) {
            if(BooleanUtil.isTrue(executorConfig.getLogCatchError())) {
                log.error("jobName: {} failure", jobName, e);
            }
            throw e;
        } finally {
            if(BooleanUtil.isTrue(executorConfig.getLogCost())) {
                val cost = System.currentTimeMillis() - start;
                log.info("jobName end: {}, cost: {}ms", jobName, cost);
            }
        }
    }

}
