package com.c332030.ctool4j.web.exception.condition;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.util.CEnumUtils;
import com.c332030.ctool4j.spring.util.CAnnotationUtils;
import com.c332030.ctool4j.web.exception.annotation.ConditionalOnMissingExceptionHandler;
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
 * Description: ConditionalOnMissingExceptionHandlerCondition
 * </p>
 *
 * @since 2026/4/9
 */
@CustomLog
@AllArgsConstructor
public class ConditionalOnMissingExceptionHandlerCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

        val annotationType = ConditionalOnMissingExceptionHandler.class;
        Class<Throwable> throwableClass = CAnnotationUtils.getAnnotationAttributeValue(
            metadata,
            annotationType,
            CEnumUtils.VALUE
        );
        Assert.notNull(throwableClass, () -> "注解不存在：" + annotationType.getName());

        val throwableClassName = throwableClass.getSimpleName();
        log.debug("ExceptionHandlerCondition matches {}", throwableClassName);

        val beanFactory = context.getBeanFactory();
        Assert.notNull(beanFactory, "beanFactory must not be null");
        val beanMap = beanFactory.getBeansWithAnnotation(ControllerAdvice.class);
        if(MapUtil.isEmpty(beanMap)) {
            log.debug("enable default @ExceptionHandler for {} because no ControllerAdvice defined", throwableClassName);
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
                    log.debug("disable default @ExceptionHandler for {} because {}.{} defined",
                        throwableClassName, beanClass.getSimpleName(), method.getName());
                    return false;
                }

            }

        }

        log.debug("enable default @ExceptionHandler for {} because no existed defined", throwableClassName);
        return true;
    }

}
