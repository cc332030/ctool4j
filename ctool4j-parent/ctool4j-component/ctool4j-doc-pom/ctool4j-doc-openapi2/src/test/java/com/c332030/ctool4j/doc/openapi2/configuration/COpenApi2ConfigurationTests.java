package com.c332030.ctool4j.doc.openapi2.configuration;

import com.c332030.ctool4j.doc.openapi2.config.CDocOpenApi2Config;
import com.c332030.ctool4j.doc.openapi2.plugins.operation.impl.COperationAnnotationPlugin;
import com.c332030.ctool4j.doc.openapi2.plugins.operation.impl.CTagAnnotationPlugin;
import com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl.CNotEmptyAnnotationPlugin;
import com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl.CParameterAnnotationPlugin;
import com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl.CRequiredAnnotationPlugin;
import com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl.CTextEnumParameterPlugin;
import com.c332030.ctool4j.doc.openapi2.plugins.property.impl.CSchemaAnnotationModelPropertyPlugin;
import com.c332030.ctool4j.doc.openapi2.plugins.property.impl.CTextEnumModelPropertyPlugin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * <p>
 * Description: COpenApi2ConfigurationTests
 * </p>
 * <p>
 * 验证 {@link COpenApi2Configuration} 十一个 {@code @Bean} 的装配契约：八个注解插件与两个
 * BeanPostProcessor 按缺省注册，Swagger {@code Docket} 按缺省注册且可被使用方覆盖。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>所有 {@code @Bean} 的行为都是"容器装配"（无条件注册、或按 {@code @ConditionalOnMissingBean} 让位），
 *   故用 {@code ApplicationContextRunner} 起最小上下文，按工厂方法名与类型断言注册结果；不引入业务启动类。</li>
 *   <li>八个注解插件逐项断言（名字 + 类型）：漏注册其中任一插件只会让对应的注解在文档里静默失效，
 *   是本类最需要逐个钉住的契约。</li>
 *   <li>{@code cDocket} 是唯一带条件的 {@code @Bean}，覆盖缺省注册与使用方覆盖两面；条件在
 *   bean 定义注册阶段评估，故"使用方配置类"必须排在 {@link COpenApi2Configuration} 之前。</li>
 *   <li>Springfox 的 {@code @Import(BeanValidatorPluginsConfiguration.class)} 随配置类一并生效，
 *   用例不重复装配它。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计（类 javadoc）：注册参数/属性/操作/分组插件与修复 handlerMappings 空指针的
 *   BeanPostProcessor；{@code Docket} 收集标注 {@code @Api} 或 {@code @CTag} 的接口。</li>
 *   <li>依据 {@code @ConditionalOnMissingBean} 的语义：按类型判断，已有同类型 bean 定义时跳过。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：八个注解插件的缺省注册；两个 BeanPostProcessor 的缺省注册；{@code Docket} 的缺省注册
 *   与使用方覆盖。</li>
 *   <li>未覆盖：各插件在真实文档生成中的注解解析效果与 Springfox 空指针修复的实际触发
 *   （由模块内插件/处理器的专项用例与集成用例覆盖）。</li>
 * </ul>
 * <h2>OpenAPI2 装配</h2>
 * <ul>
 *   <li>1.1 八个注解插件按缺省注册（{@code cAnnotationPlugins_defaultsRegistered}）</li>
 *   <li>1.2 两个 BeanPostProcessor 按缺省注册（{@code cBeanPostProcessors_registered}）</li>
 *   <li>2.1 缺省注册 Swagger {@code Docket}（{@code cDocket_defaultsRegistered}）</li>
 *   <li>2.2 使用方提供 {@code Docket} 时默认实现让位（{@code cDocket_userBeanWins}）</li>
 * </ul>
 *
 * @since 2026/9/23
 * @version 1.0
 * @see COpenApi2Configuration
 */
class COpenApi2ConfigurationTests {

    /**
     * 对应测试用例 1.1：八个注解插件按缺省注册
     */
    @Test
    void cAnnotationPlugins_defaultsRegistered() {
        runner().run(context -> {
            // 正例：参数 / 属性 / 操作 三族插件逐个按工厂方法名与类型注册
            assertBeanOfType(context, "cExpanderNotEmpty", CNotEmptyAnnotationPlugin.class);
            assertBeanOfType(context, "cExpanderCRequired", CRequiredAnnotationPlugin.class);
            assertBeanOfType(context, "cModelPropertyCSchema", CSchemaAnnotationModelPropertyPlugin.class);
            assertBeanOfType(context, "cModelPropertyTextEnum", CTextEnumModelPropertyPlugin.class);
            assertBeanOfType(context, "cParameterTextEnum", CTextEnumParameterPlugin.class);
            assertBeanOfType(context, "cOperationCOperation", COperationAnnotationPlugin.class);
            assertBeanOfType(context, "cOperationCTag", CTagAnnotationPlugin.class);
            assertBeanOfType(context, "cParameterCParameter", CParameterAnnotationPlugin.class);
        });
    }

    /**
     * 对应测试用例 1.2：两个 BeanPostProcessor 按缺省注册
     */
    @Test
    void cBeanPostProcessors_registered() {
        runner().run(context -> {
            // 正例：空分组清理与 springfox handlerMappings 修复都由静态 @Bean 注册
            assertBeanOfType(context, "cEmptyTagBeanPostProcessor", BeanPostProcessor.class);
            assertBeanOfType(context, "cSpringfoxHandlerProviderBeanPostProcessor", BeanPostProcessor.class);
        });
    }

    /**
     * 对应测试用例 2.1：缺省注册 Swagger {@code Docket}
     */
    @Test
    void cDocket_defaultsRegistered() {
        runner().run(context -> {
            // 正例：无自定义 Docket 时按缺省装配，且 Bean 名即工厂方法名
            Assertions.assertArrayEquals(
                new String[] {"cDocket"},
                context.getBeanNamesForType(Docket.class)
            );
            Assertions.assertInstanceOf(Docket.class, context.getBean(Docket.class));
        });
    }

    /**
     * 对应测试用例 2.2：使用方提供 {@code Docket} 时默认实现让位
     */
    @Test
    void cDocket_userBeanWins() {
        // 使用方配置类排在前面：条件评估时其 bean 定义已存在 → 默认实现跳过
        new ApplicationContextRunner()
            .withUserConfiguration(UserDocketConfig.class, COpenApi2Configuration.class)
            .withBean(CDocOpenApi2Config.class)
            .run(context -> {
                Assertions.assertArrayEquals(
                    new String[] {"userDocket"},
                    context.getBeanNamesForType(Docket.class)
                );
            });
    }

    /**
     * 构造加载 {@link COpenApi2Configuration} 的运行器（文档配置以普通 Bean 提供）
     *
     * @return 上下文运行器
     */
    private static ApplicationContextRunner runner() {
        return new ApplicationContextRunner()
            .withUserConfiguration(COpenApi2Configuration.class)
            .withBean(CDocOpenApi2Config.class);
    }

    /**
     * 断言指定 Bean 名已注册且类型匹配
     *
     * @param context  应用上下文
     * @param beanName Bean 名（即 {@code @Bean} 工厂方法名）
     * @param type     期望类型
     */
    private static void assertBeanOfType(ApplicationContext context, String beanName, Class<?> type) {
        Assertions.assertTrue(context.containsBean(beanName));
        Assertions.assertInstanceOf(type, context.getBean(beanName));
    }

    /**
     * 使用方自建 {@code Docket} 的配置（验证条件不覆盖使用方实现）
     */
    @Configuration
    static class UserDocketConfig {

        /**
         * 使用方 Docket
         *
         * @return Docket
         */
        @Bean
        Docket userDocket() {
            return new Docket(DocumentationType.SWAGGER_2);
        }

    }

}
