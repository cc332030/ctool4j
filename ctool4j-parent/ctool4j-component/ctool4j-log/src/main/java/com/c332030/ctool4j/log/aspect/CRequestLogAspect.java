package com.c332030.ctool4j.log.aspect;

import com.c332030.ctool4j.log.util.RequestLogUtils;
import com.c332030.ctool4j.spring.util.CAspectUtils;
import lombok.CustomLog;
import lombok.Lombok;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

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
public class CRequestLogAspect {

    @Pointcut(
            "@annotation(org.springframework.web.bind.annotation.RequestMapping)"
                    + " || @annotation(org.springframework.web.bind.annotation.GetMapping)"
                    + " || @annotation(org.springframework.web.bind.annotation.PostMapping)"
                    + " || @annotation(org.springframework.web.bind.annotation.PutMapping)"
                    + " || @annotation(org.springframework.web.bind.annotation.PatchMapping)"
                    + " || @annotation(org.springframework.web.bind.annotation.DeleteMapping)"
    )
    public void annotationPointcut(){}

    @Around("annotationPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) {

        Object result = null;
        Throwable throwable = null;
        try {

            val args = joinPoint.getArgs();

            val method = CAspectUtils.getMethod(joinPoint);
            val parameters = method.getParameters();

            val argMap = new LinkedHashMap<String, Object>(parameters.length);
            for (int i = 0; i < parameters.length; i++) {
                val parameter = parameters[i];
                argMap.put(parameter.getName(), args[i]);
            }

            RequestLogUtils.init(argMap);

            result = joinPoint.proceed(args);
            return result;
        } catch (Throwable e) {
            throwable = e;
            throw Lombok.sneakyThrow(e);
        } finally {
            RequestLogUtils.write(result, throwable);
        }
    }

}
