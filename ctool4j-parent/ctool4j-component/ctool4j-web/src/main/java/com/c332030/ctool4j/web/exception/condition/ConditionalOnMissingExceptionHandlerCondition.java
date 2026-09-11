package com.c332030.ctool4j.web.exception.condition;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import com.c332030.ctool4j.core.classes.CReflectUtils;
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
 * <h2>能力目录</h2>
 * <p>核心方法 {@code matches(ConditionContext, AnnotatedTypeMetadata)}：</p>
 * <ul>
 *   <li>从注解元数据读取要检查的异常类型 {@code value()}</li>
 *   <li>取容器中所有 {@code ControllerAdvice} 标注的 bean</li>
 *   <li>无任何 ControllerAdvice 时返回 true（启用默认 Handler）</li>
 *   <li>遍历各 bean 方法，若任一方法用 {@code @ExceptionHandler} 处理了该异常类型，返回 false（禁用默认 Handler）</li>
 *   <li>均未处理时返回 true（启用默认 Handler）</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>注解不存在</td>
 *     <td>{@code Assert.notNull} 抛异常</td>
 *   </tr>
 *   <tr>
 *     <td>beanFactory 为 null</td>
 *     <td>{@code Assert.notNull} 抛异常</td>
 *   </tr>
 *   <tr>
 *     <td>无 ControllerAdvice</td>
 *     <td>返回 true，启用默认 Handler</td>
 *   </tr>
 *   <tr>
 *     <td>bean 无方法</td>
 *     <td>跳过该 bean，继续遍历</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>框架内置异常处理器的条件装配，避免与业务方自定义处理器冲突。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>用反射遍历 bean 方法，性能开销可接受（仅 Spring 启动装配时执行一次）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>反射遍历</b></p>
 * <ul>
 *   <li>用 {@code CReflectUtils.getMethods} 获取 bean 方法，检查 {@code @ExceptionHandler} 注解的 {@code value()} 是否包含目标异常类型。</li>
 * </ul>
 * <p><b>空安全</b></p>
 * <ul>
 *   <li>beanFactory/beanMap 为空时按"无 ControllerAdvice"处理，返回 true 启用默认 Handler。</li>
 * </ul>
 *
 * @since 2026/4/9
 * @version 1.0
 */
@CustomLog
@AllArgsConstructor
public class ConditionalOnMissingExceptionHandlerCondition implements Condition {

    /**
     * 匹配条件：无任何 ControllerAdvice 或均未处理该异常类型时返回 true
     *
     * @param context  条件上下文
     * @param metadata 注解元数据
     * @return 是否满足条件
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

        val annotationType = ConditionalOnMissingExceptionHandler.class;
        Class<Throwable> throwableClass = CAnnotationUtils.getAnnotationValue(
            metadata,
            annotationType
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
