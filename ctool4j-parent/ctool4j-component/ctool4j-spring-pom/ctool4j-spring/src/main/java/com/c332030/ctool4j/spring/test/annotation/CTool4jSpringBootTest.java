package com.c332030.ctool4j.spring.test.annotation;

import com.c332030.ctool4j.spring.configuration.CSpringConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CTool4jSpringBootTest：{@code @SpringBootTest} + {@code @EnableAutoConfiguration} 的组合注解，
 * 用于启动 Spring 容器测试
 * </p>
 *
 * <p>默认加载 {@link CSpringConfiguration}（组件扫描 {@code com.c332030.ctool4j} + 共享 RestTemplate），
 * MOCK web 环境；需要追加测试用类（如测试 controller）时通过 {@link #classes()} 指定，
 * 需要配置 web 环境时通过 {@link #webEnvironment()} 指定，需要做 MVC 接口测试时在测试类上再叠加
 * {@code @AutoConfigureMockMvc}</p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CTool4jSpringBootTest}：容器测试启动注解。</p>
 * <ul>
 *   <li>启动配置：内嵌 {@code CTestConfiguration}（{@code @SpringBootConfiguration} + 引入框架配置）。</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>单独提供内嵌启动类而不复用 {@link CSpringConfiguration}：{@code @SpringBootTest#classes}
 *   需存在 {@code @SpringBootConfiguration} 才不报错，而业务配置类只承担配置职责。</li>
 *   <li>扫描范围由 {@code CSpringUtils.getBasePackages()} 自行兜底（框架基础包 + 启动类所在包），
 *   测试无需为扫描范围额外声明。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>容器内无启动类</td>
 *     <td>扫描范围按框架基础包兜底，不中断容器启动</td>
 *   </tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>ctool4j 各模块内的 Spring 容器测试。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>测试专用注解，不进入生产装配；非 Spring 的纯单元测试不适用。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>补包为进程级生效，测试并行时相互可见（只影响扫描范围，不影响正确性）。</li>
 *   <li>补包使扫描面扩大，容器启动开销略高于最小装配。</li>
 * </ul>
 *
 * @since 2025/12/28
 * @version 1.0
 * @see CSpringConfiguration
 */
@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)

@EnableAutoConfiguration
@SpringBootTest(classes = {CTool4jSpringBootTest.CTestConfiguration.class})
public @interface CTool4jSpringBootTest {

    /**
     * 测试容器配置：以 {@code @SpringBootConfiguration} 承担「启动类」角色，并引入
     * {@link CSpringConfiguration} 完成组件扫描与属性扫描
     */
    @SpringBootConfiguration
    @Import(CSpringConfiguration.class)
    class CTestConfiguration {

    }

    /**
     * 加载的配置/测试用类（默认 {@link CSpringConfiguration}，作为配置源；不显式指定时
     * 需依赖该默认值，否则 {@code @SpringBootTest} 会因找不到 {@code @SpringBootConfiguration} 而报错）
     *
     * @return 加载的类
     */
    @AliasFor(annotation = SpringBootTest.class, attribute = "classes")
    Class<?>[] classes() default {CTool4jSpringBootTest.CTestConfiguration.class};

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
