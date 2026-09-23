package com.c332030.ctool4j.spring.annotation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * Description: CConfigurationPropertiesBindingTests
 * </p>
 *
 * <p>
 * 是 {@link CConfigurationProperties} 的配置绑定集成测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>本注解的承诺是「配置里多出一个未知键、或某个键的取值类型写错，都不会让应用起不来」，
 *   故用例从容器启动这件事本身取证：用 {@code ApplicationContextRunner} 起一个最小上下文，断言启动失败与否。</li>
 *   <li>不用断言注解属性值的方式代替本用例：属性值对了不等于绑定阶段真的忽略——两者是不同的观测点，
 *   前者由 {@code CConfigurationPropertiesTests} 覆盖，后者只能由真实绑定链路覆盖。</li>
 *   <li>两个开关各取一条正例：未知键忽略沿用元注解默认值，非法值忽略由元注解使用点上的 {@code true} 承担——
 *   两条都要证明「承诺的默认行为确实发生」。</li>
 *   <li>不设「显式关闭」的反例：本注解只有一个属性元素、承载不了该开关，用元注解写反例测到的只是元注解的结论。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据类级「兜底设计」：未知键忽略沿用元注解默认值，非法属性忽略由元注解使用点声明为 {@code true}。</li>
 *   <li>依据 Spring Boot 官方文档：{@code ignoreUnknownFields} / {@code ignoreInvalidFields} 为 {@code false} 时的绑定失败语义。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：未知键被忽略（应用正常启动且已知键绑定成功）、非法值被忽略（应用正常启动且其余键照常绑定）。</li>
 *   <li>未覆盖：强校验下的启动失败（本注解不承载该开关，须改用元注解，测它不是本注解的结论）；
 *   属性扫描（{@code @ConfigurationPropertiesScan}）自动注册属性 Bean 的链路——本用例用
 *   {@code @EnableConfigurationProperties} 显式启用，避免与扫描配置耦合。</li>
 * </ul>
 * <h2>用例目录</h2>
 * <ul>
 *   <li>1.1 未知键被忽略且已知键绑定成功（{@code unknownKeyIgnored}）</li>
 *   <li>1.2 非法值被忽略（{@code invalidValueIgnored}）</li>
 * </ul>
 *
 * @since 2026/9/21
 * @version 1.6
 * @see CConfigurationProperties
 * @see CConfigurationPropertiesTests
 */
public class CConfigurationPropertiesBindingTests {

    /**
     * 默认配置属性类：未知键与非法值均被忽略（默认行为）
     */
    @CConfigurationProperties("c-binding-test")
    static class DefaultConfig {

        /**
         * 已知键，用于验证忽略未知键的同时不丢已知键
         */
        private String name;

        /**
         * 数值键，用于验证非法值被忽略
         */
        private Integer size;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }
    }

    /**
     * 启用默认配置属性类的测试上下文
     */
    @Configuration
    @EnableConfigurationProperties(DefaultConfig.class)
    static class EnableDefault {

    }

    /**
     * 对应测试用例 1.1：未知键被忽略且已知键绑定成功
     *
     * <p>未知键由元注解 {@code ignoreUnknownFields} 的默认 {@code true} 忽略，非本注解所声明；
     * 本用例测量的是「本注解声明下默认行为仍是忽略未知键」，不测量该默认值的来源。</p>
     */
    @Test
    public void unknownKeyIgnored() {

        new ApplicationContextRunner()
            .withPropertyValues(
                "c-binding-test.name=ctool4j",
                "c-binding-test.unknown-key=whatever"
            )
            .withUserConfiguration(EnableDefault.class)
            .run(context -> {
                Assertions.assertNull(context.getStartupFailure(),
                    "未知键不应让应用启动失败（元注解 ignoreUnknownFields 默认 true）");
                Assertions.assertEquals("ctool4j", context.getBean(DefaultConfig.class).getName(),
                    "忽略未知键的同时，已知键仍须正常绑定");
            });
    }

    /**
     * 对应测试用例 1.2：非法值被忽略
     *
     * <p>类型不匹配的属性按本注解的元注解使用点 {@code ignoreInvalidFields = true} 被跳过，
     * 应用照常启动、其余键照常绑定。</p>
     */
    @Test
    public void invalidValueIgnored() {

        new ApplicationContextRunner()
            .withPropertyValues(
                "c-binding-test.name=ctool4j",
                "c-binding-test.size=not-a-number"
            )
            .withUserConfiguration(EnableDefault.class)
            .run(context -> {
                Assertions.assertNull(context.getStartupFailure(),
                    "非法值不应让应用启动失败（ignoreInvalidFields 由元注解使用点声明为 true）");
                Assertions.assertNull(context.getBean(DefaultConfig.class).getSize(),
                    "被忽略的非法值不绑定，属性保持默认 null");
                Assertions.assertEquals("ctool4j", context.getBean(DefaultConfig.class).getName(),
                    "忽略非法值的同时，其余键仍须正常绑定");
            });
    }

}
