package com.c332030.ctool4j.web.exception.annotation;

import com.c332030.ctool4j.web.exception.condition.ConditionalOnMissingExceptionHandlerCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * <p>
 * Description: ConditionalOnMissingExceptionHandler
 * </p>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>无</td>
 *     <td>注解元数据，行为在 Condition 中</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>框架内置异常处理器标注该注解，避免与业务方自定义处理器冲突。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>需配合 {@code ConditionalOnMissingExceptionHandlerCondition} 才生效。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>条件装配</b></p>
 * <ul>
 *   <li>当容器中无任何 {@code ControllerAdvice} 处理指定异常类型时，被标注的 Handler 才生效</li>
 *   <li>（判断逻辑见 {@code ConditionalOnMissingExceptionHandlerCondition}）。</li>
 * </ul>
 *
 * @since 2026/4/22
 * @version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Conditional(ConditionalOnMissingExceptionHandlerCondition.class)
public @interface ConditionalOnMissingExceptionHandler {

    /**
     * The exception type to check.
     *
     * @return the exception class
     */
    Class<? extends Throwable> value();

}
