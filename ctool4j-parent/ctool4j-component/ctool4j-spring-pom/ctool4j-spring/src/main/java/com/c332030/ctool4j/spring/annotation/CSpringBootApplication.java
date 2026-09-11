package com.c332030.ctool4j.spring.annotation;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CSpringBootApplication
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CSpringBootApplication}：SpringBoot 启动注解。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>@SpringBootApplication + @ConfigurationPropertiesScan 组合，exclude 别名</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>未配置 exclude 为空</p>
 * <h2>适用范围</h2>
 * <p>应用启动类声明</p>
 * <h2>不适用与边界场景</h2>
 * <p>exclude 转发给 SpringBootApplication</p>
 *
 * @since 2025/11/10
 * @version 1.0
 */
@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)

@SpringBootApplication
@ConfigurationPropertiesScan
public @interface CSpringBootApplication {

    @AliasFor(annotation = SpringBootApplication.class)
    Class<?>[] exclude() default {};

}
