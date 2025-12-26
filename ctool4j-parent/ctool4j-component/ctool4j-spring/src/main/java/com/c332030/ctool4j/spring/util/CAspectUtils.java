package com.c332030.ctool4j.spring.util;

import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * <p>
 * Description: CAspectUtils
 * </p>
 *
 * @author c332030
 * @since 2024/5/6
 */
@CustomLog
@UtilityClass
public class CAspectUtils {

    /**
     * 获取切点方法
     * @param joinPoint 切点
     * @return 切点方法
     */
    public Method getMethod(ProceedingJoinPoint joinPoint) {
        val signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod();
    }

    @SneakyThrows
    public Object process(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed(joinPoint.getArgs());
        } catch (UndeclaredThrowableException e) {
            log.debug("process failure", e);
            throw e.getUndeclaredThrowable();
        }
    }

}
