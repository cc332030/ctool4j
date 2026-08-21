package com.c332030.ctool4j.spring.test.annotation;

import com.c332030.ctool4j.spring.configuration.CSpringConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CTool4jSpringBootTest：@SpringBootTest(CSpringConfiguration) + @EnableAutoConfiguration 的组合注解，
 * 用于启动 Spring 容器测试
 * </p>
 *
 * <p>
 * 默认加载 {@link CSpringConfiguration}（组件扫描 com.c332030.ctool4j + RestTemplate），
 * MOCK web 环境；需要追加测试用类（如测试 controller）时通过 {@link #classes()} 指定，
 * 需要配置 web 环境时通过 {@link #webEnvironment()} 指定，需要做 MVC 接口测试时在测试类上再叠加
 * {@code @AutoConfigureMockMvc}
 * </p>
 *
 * @since 2025/12/28
 */
@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)

@EnableAutoConfiguration
@SpringBootTest(classes = {CSpringConfiguration.class})
public @interface CTool4jSpringBootTest {

    /**
     * 加载的配置/测试用类（默认 {@link CSpringConfiguration}，作为配置源；不显式指定时
     * 需依赖该默认值，否则 @SpringBootTest 会因找不到 @SpringBootConfiguration 而报错）
     *
     * @return 加载的类
     */
    @AliasFor(annotation = SpringBootTest.class, attribute = "classes")
    Class<?>[] classes() default {CSpringConfiguration.class};

    /**
     * web 环境（默认 MOCK）
     *
     * @return web 环境
     */
    @AliasFor(annotation = SpringBootTest.class, attribute = "webEnvironment")
    WebEnvironment webEnvironment() default WebEnvironment.MOCK;

    /**
     * 排除的自动配置类
     *
     * @return 排除的自动配置类
     */
    @AliasFor(annotation = EnableAutoConfiguration.class, attribute = "exclude")
    Class<?>[] exclude() default {};

}
