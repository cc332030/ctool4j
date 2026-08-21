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
 * Description: CLogAndIgnoreThrowableAspect
 * </p>
 *
 * @since 2025/12/21
 * @see doc/design/spring/CLogAndIgnoreThrowableAspect.adoc
 */
@CustomLog
@Aspect
@Component
@AllArgsConstructor
public class CLogAndIgnoreThrowableAspect {

    /**
     * 标注了 CLogAndIgnoreThrowable 注解的方法切点
     */
    @Pointcut("@annotation(com.c332030.ctool4j.spring.exception.annotation.CLogAndIgnoreThrowable)")
    public void annotationPointcut() {}

    /**
     * 环绕增强，记录被标注方法抛出的异常并吞掉
     *
     * @param joinPoint 切点
     * @return 切点方法的返回值；异常时返回 null
     */
    @SneakyThrows
    @Around("annotationPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) {

        try {
            return CAspectUtils.process(joinPoint);
        } catch (Throwable t) {

            val method = CAspectUtils.getMethod(joinPoint);
            log.error("log Throwable on method: {}", method.getName(), t);
        }

        return null;
    }

}
