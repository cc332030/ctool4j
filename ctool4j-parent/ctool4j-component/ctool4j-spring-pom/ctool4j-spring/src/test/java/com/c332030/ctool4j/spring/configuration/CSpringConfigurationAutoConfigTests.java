package com.c332030.ctool4j.spring.configuration;

import com.c332030.ctool4j.core.jackson.CJacksonUtils;
import com.c332030.ctool4j.spring.util.CRestTemplateUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestConstructor;
import org.springframework.web.client.RestTemplate;

/**
 * <p>
 * Description: CSpringConfigurationAutoConfigTests
 * </p>
 *
 * <p>
 * 是 {@link CSpringConfiguration} 的自动配置装配路径测试用例，覆盖其两个 {@code @Bean}
 * 在容器内的实际来源与实例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>{@code CSpringConfiguration} 的两个 {@code @Bean} 都带 {@code @ConditionalOnMissingBean}：
 *   与 Spring Boot 的 {@code JacksonAutoConfiguration} 等自动配置声明同一类型时，谁生效取决于
 *   自动配置的处理顺序（先注册者胜出），属隐式约定；本用例把该约定固化为可观测的断言，
 *   顺序一旦翻转即失败，而不是静默改用 Spring Boot 构建的 Bean。</li>
 *   <li>刻意不使用 {@code CTool4jSpringBootTest}：该注解经 {@code @Import(CSpringConfiguration.class)}
 *   把配置类引入常规配置类处理阶段，必然先于所有自动配置注册 Bean 定义，会掩盖真实竞争；
 *   这里仅用 {@code @EnableAutoConfiguration}，复现生产装配路径（业务方经 {@code CSpringBootApplication}
 *   启动，其 {@code @ComponentScan} 只覆盖业务包，扫描不到 {@code com.c332030.ctool4j}，
 *   框架配置只能由 {@code AutoConfiguration.imports} 的自动配置引入）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计：全局 JSON 输出与出站 HTTP 调用都使用共享的 {@code CJacksonUtils.OBJECT_MAPPER}
 *   （Long/BigDecimal 序列化为字符串防止前端溢出），容器内不应出现 Spring Boot 默认构建的 mapper。</li>
 *   <li>依据黑盒原则：只断言容器内可观测的 Bean 名称与实例，不假设自动配置的排序实现细节。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：自动配置装配路径下，两个 {@code @Bean} 均生效、实例为其共享常量、Bean 名即工厂方法名。</li>
 *   <li>未覆盖：使用方自行声明同类型 Bean 时以使用方为准（由 {@code @ConditionalOnMissingBean} 语义保证）。</li>
 * </ul>
 * <h2>容器内共享 Bean</h2>
 * <ul>
 *   <li>1 ObjectMapper：1.1 共享实例（{@code objectMapper_sharedInstance}）、1.2 定义来源（{@code objectMapper_beanName}）</li>
 *   <li>2 RestTemplate：2.1 共享实例（{@code restTemplate_sharedInstance}）、2.2 定义来源（{@code restTemplate_beanName}）</li>
 * </ul>
 *
 * @since 2026/9/23
 * @version 1.0
 * @see CSpringConfiguration
 */
@AllArgsConstructor
@SpringBootTest(classes = CSpringConfigurationAutoConfigTests.CApplication.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class CSpringConfigurationAutoConfigTests {

    /**
     * 测试用启动类：仅开启自动配置、不引入 {@link CSpringConfiguration}，
     * 使其只能经 {@code AutoConfiguration.imports} 自动配置引入，复现生产装配路径
     */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class CApplication {

    }

    /**
     * 应用上下文
     */
    ApplicationContext applicationContext;

    /**
     * 对应测试用例 1.1：容器 ObjectMapper 为共享实例
     *
     * <p>断言取舍：用 {@code assertSame} 而非 {@code assertNotNull}——只有"两者是同一实例"
     * 才能排除 Spring Boot 另行构建一个 mapper 的可能，弱断言区分不了这两种情况。</p>
     */
    @Test
    public void objectMapper_sharedInstance() {

        Assertions.assertSame(
            CJacksonUtils.OBJECT_MAPPER,
            applicationContext.getBean(ObjectMapper.class)
        );
    }

    /**
     * 对应测试用例 1.2：容器 ObjectMapper 定义来自 cObjectMapper
     *
     * <p>断言按 Bean 定义来源而非实例：实例相同只能说明"用了共享常量"，
     * 名称则直接定位是哪个工厂方法注册的——被 {@code JacksonAutoConfiguration} 抢先注册时
     * 这里会得到 {@code jacksonObjectMapper}。</p>
     */
    @Test
    public void objectMapper_beanName() {

        val names = applicationContext.getBeanNamesForType(ObjectMapper.class);

        Assertions.assertArrayEquals(new String[] {"cObjectMapper"}, names);
    }

    /**
     * 对应测试用例 2.1：容器 RestTemplate 为共享实例
     *
     * <p>断言取舍：用 {@code assertSame} 而非 {@code assertNotNull}——共享实例是"出站调用统一
     * 使用项目内置 Jackson 转换器"的前提，仅非空无法证明这一点。</p>
     */
    @Test
    public void restTemplate_sharedInstance() {

        Assertions.assertSame(
            CRestTemplateUtils.REST_TEMPLATE,
            applicationContext.getBean(RestTemplate.class)
        );
    }

    /**
     * 对应测试用例 2.2：容器 RestTemplate 定义来自 cRestTemplate
     *
     * <p>该 {@code @Bean} 标了 {@code @Lazy}：按下名称断言不触发实例化（类型可由工厂方法签名推断），
     * 故本用例与 2.1 各自独立成立，不会因"取实例"而互相掩盖。</p>
     */
    @Test
    public void restTemplate_beanName() {

        val names = applicationContext.getBeanNamesForType(RestTemplate.class);

        Assertions.assertArrayEquals(new String[] {"cRestTemplate"}, names);
    }

}
