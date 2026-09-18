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
 *   <li>异常类型为容器私有或可选依赖（如 Tomcat 的 {@code ClientAbortException}）时用 {@link #valueName()}，跨容器不失败。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>需配合 {@code ConditionalOnMissingExceptionHandlerCondition} 才生效。</li>
 *   <li>{@link #value()} 与 {@link #valueName()} 二选一；同时配置时以 {@link #valueName()} 为准。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>条件装配</b></p>
 * <ul>
 *   <li>当容器中无任何 {@code ControllerAdvice} 处理指定异常类型时，被标注的 Handler 才生效</li>
 *   <li>（判断逻辑见 {@code ConditionalOnMissingExceptionHandlerCondition}）。</li>
 * </ul>
 * <p><b>两类引用方式</b></p>
 * <ul>
 *   <li>{@link #value()}：类可直接引用时用（类型比较）。</li>
 *   <li>{@link #valueName()}：按<b>类名字符串</b>比较，不解析类——用于在部分环境不存在的类型，
 *   避免读取注解属性时因类加载失败导致应用无法启动（如用 Jetty 时的 Tomcat 专有异常）。</li>
 * </ul>
 *
 * @since 2026/4/22
 * @version 1.1
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Conditional(ConditionalOnMissingExceptionHandlerCondition.class)
public @interface ConditionalOnMissingExceptionHandler {

    /**
     * 待检查的异常类型
     * <p>与 {@link #valueName()} 二选一：类可直接引用时用本属性；容器私有/可选依赖的异常类型用 {@link #valueName()}</p>
     *
     * @return 异常类型
     */
    Class<? extends Throwable> value() default Throwable.class;

    /**
     * 待检查异常类型的全限定类名
     * <p>按类名字符串比较、<b>不解析类</b>，用于容器私有或可选依赖的异常类型，
     * 避免在缺失该类的环境（如无 Tomcat 的 Jetty/Undertow）因类加载失败而无法启动</p>
     *
     * @return 异常类型全限定名；未配置时为空串
     */
    String valueName() default "";

}
