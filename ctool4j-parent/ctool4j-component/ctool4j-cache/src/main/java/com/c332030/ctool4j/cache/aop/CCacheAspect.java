package com.c332030.ctool4j.cache.aop;

import com.c332030.ctool4j.cache.annotation.CCacheable;
import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.spring.util.CAspectUtils;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Description: CCacheAspect
 * </p>
 *
 * @since 2025/9/27
 */
@Aspect
public class CCacheAspect {

    private static final CClassValue<Map<String, Object>> CLASS_CACHE_VALUE = CClassValue
        .of(e -> new ConcurrentHashMap<>());

    @Around("@annotation(com.c332030.ctool4j.cache.annotation.CCacheable)")
    public Object cacheableInterceptor(ProceedingJoinPoint joinPoint) throws Throwable {

        val method = CAspectUtils.getMethod(joinPoint);
        val cCacheable = CReflectUtils.getAnnotationCached(method, CCacheable.class);
        if(null != cCacheable) {

            val namespace = cCacheable.namespace();
            if(cCacheable.local()) {
                val cacheMap = CLASS_CACHE_VALUE.get(namespace);
            }

        }

        return CAspectUtils.process(joinPoint);
    }

}
