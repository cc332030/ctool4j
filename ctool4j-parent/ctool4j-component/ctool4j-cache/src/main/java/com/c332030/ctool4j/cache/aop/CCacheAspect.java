package com.c332030.ctool4j.cache.aop;

import com.c332030.ctool4j.cache.annotation.CCacheable;
import com.c332030.ctool4j.spring.util.CAspectUtils;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * <p>
 * Description: CCacheAspect
 * </p>
 *
 * @since 2025/9/27
 */
@Aspect
public class CCacheAspect {

    @Around("@annotation(com.c332030.ctool4j.cache.annotation.CCacheable)")
    public Object serviceCacheableInterceptor(ProceedingJoinPoint joinPoint) throws Throwable {

        val signature = joinPoint.getSignature();
        // 方法注解
        if (signature instanceof MethodSignature) {
            val methodSignature = (MethodSignature) signature;
            val targetMethod = methodSignature.getMethod();
            val cacheable = targetMethod.getAnnotation(CCacheable.class);
        }
        return CAspectUtils.process(joinPoint);
    }

}
