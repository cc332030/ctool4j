package com.c332030.ctool4j.spring.exception.aspect;

import com.c332030.ctool4j.spring.util.CAspectUtils;
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
 * Description: CCatchAndLogThrowableAspect
 * </p>
 *
 * @since 2025/12/21
 */
@CustomLog
@Aspect
@Component
@AllArgsConstructor
public class CCatchAndLogThrowableAspect {

    @Pointcut("@annotation(com.c332030.ctool4j.spring.exception.annotation.CCatchAndLogThrowable)")
    public void annotationPointcut() {}

    @SneakyThrows
    @Around("annotationPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) {

        val method = CAspectUtils.getMethod(joinPoint);
        try {
            return joinPoint.proceed(joinPoint.getArgs());
        } catch (Throwable t) {
            log.error("log Throwable on method: {}", method.getName(), t);
        }

        return null;
    }

}
