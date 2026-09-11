package com.c332030.ctool4j.spring.annotation;

import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CLazyService
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CLazyService}：懒加载服务注解。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>@Service + @Lazy + @Component 组合，value 别名 Service.value</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>未指定值默认空串</p>
 * <h2>适用范围</h2>
 * <p>懒加载服务Bean声明</p>
 * <h2>不适用与边界场景</h2>
 * <p>仅标注类型</p>
 * <h2>已知限制与取舍</h2>
 * <p>仅标注类型</p>
 *
 * @since 2026/5/14
 * @version 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component

@Lazy
@Service
public @interface CLazyService {

    @AliasFor(annotation = Service.class)
    String value() default "";

}
