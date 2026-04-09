package com.c332030.ctool4j.web.exception.handler;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.val;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CAbstractMissingExceptionHandlerCondition
 * </p>
 *
 * @since 2026/4/9
 */
@CustomLog
@AllArgsConstructor
public abstract class CAbstractMissingExceptionHandlerCondition<T extends Throwable> implements Condition {

    Class<T> throwableClass;

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

        log.debug("CAbstractMissingExceptionHandlerCondition matches {}", throwableClass);

        val beanFactory = context.getBeanFactory();
        Assert.notNull(beanFactory, "beanFactory must not be null");
        val beanMap = beanFactory.getBeansWithAnnotation(ControllerAdvice.class);
        if(MapUtil.isEmpty(beanMap)) {
            log.info("enable default @ExceptionHandler for {} because no ControllerAdvice defined", throwableClass);
            return true;
        }

        val beans = beanMap.values();
        for (val bean : beans) {

            val beanClass = bean.getClass();
            val methods = CReflectUtils.getMethods(beanClass);
            if(ArrayUtil.isEmpty(methods)) {
                continue;
            }

            for (val method : methods) {

                val annotations = method.getAnnotationsByType(ExceptionHandler.class);
                if(ArrayUtil.isEmpty(annotations)) {
                    continue;
                }

                val annotationValues = Arrays.stream(annotations)
                    .map(ExceptionHandler::value)
                    .map(Arrays::asList)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
                if(annotationValues.contains(throwableClass)) {
                    log.info("disable default @ExceptionHandler for {} because {}.{} defined",
                        throwableClass, beanClass.getSimpleName(), method.getName());
                    return false;
                }

            }

        }

        log.info("enable default @ExceptionHandler for {} because no existed defined", throwableClass);
        return true;
    }

}
