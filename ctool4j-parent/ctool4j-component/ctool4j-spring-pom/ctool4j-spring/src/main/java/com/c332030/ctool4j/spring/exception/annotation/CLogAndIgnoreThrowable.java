package com.c332030.ctool4j.spring.exception.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CLogAndIgnoreThrowable
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CLogAndIgnoreThrowable}：日志并忽略异常注解。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>标注后记录日志并忽略抛出的异常</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>异常日志与忽略</p>
 * <h2>不适用与边界场景</h2>
 * <p>配合切面</p>
 * <h2>已知限制与取舍</h2>
 * <p>配合切面</p>
 *
 * @since 2025/12/21
 * @version 1.0
 */
@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CLogAndIgnoreThrowable {

}
