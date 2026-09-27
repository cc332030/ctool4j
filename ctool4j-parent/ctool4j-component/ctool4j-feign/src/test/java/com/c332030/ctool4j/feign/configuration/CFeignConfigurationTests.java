package com.c332030.ctool4j.feign.configuration;

import com.c332030.ctool4j.feign.config.CFeignClientLogConfig;
import com.c332030.ctool4j.feign.interceptor.CFeignInterceptor;
import com.c332030.ctool4j.feign.log.CFeignLogger;
import feign.Logger;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * Description: CFeignConfigurationTests
 * </p>
 * <p>
 * 验证 {@link CFeignConfiguration} 三个 {@code @Bean} 的装配契约：拦截器无条件注册；日志级别与日志实现
 * 按缺省注册（{@code @ConditionalOnMissingBean}），使用方提供同类型 Bean 时让位。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>三个 {@code @Bean} 的行为都是"容器装配"，故用 {@code ApplicationContextRunner} 起最小上下文，
 *   按 Bean 名与类型断言注册结果；不引入业务启动类。</li>
 *   <li>两个带 {@code @ConditionalOnMissingBean} 的方法各覆盖两面：缺省时注册默认实现、使用方已提供时跳过。
 *   条件在<b>bean 定义注册阶段</b>评估，故"使用方配置类"必须排在 {@link CFeignConfiguration} 之前
 *   （{@code withUserConfiguration} 的实参顺序即注册顺序），否则条件感知不到使用方定义。</li>
 *   <li>默认级别 FULL 是安全相关取舍（会打印请求/响应全文），单列一例固化，避免它被静默改弱为 NONE。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计（类 javadoc）：拦截器默认注册；日志级别/日志实现按缺省注册、可被 {@code @ConditionalOnMissingBean} 覆盖。</li>
 *   <li>依据 {@code @ConditionalOnMissingBean} 的语义：按类型判断，已有同类型 bean 定义时跳过。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：拦截器注册；日志级别与日志实现的缺省注册；使用方 Bean 覆盖两者的条件分支。</li>
 *   <li>未覆盖：被注释掉的 {@code cFeignClient} 装配（当前不是 Bean）；日志实现的实际输出格式与过滤行为
 *   （由 {@code CFeignLoggerTests} 覆盖）。</li>
 * </ul>
 * <h2>Feign 装配</h2>
 * <ul>
 *   <li>1.1 拦截器无条件注册为 Bean（{@code cFeignInterceptor_registeredAsBean}）</li>
 *   <li>2.1 缺省日志级别为 FULL（{@code cFeignLoggerLevel_defaultsToFull}）</li>
 *   <li>2.2 使用方提供日志级别时默认实现让位（{@code cFeignLoggerLevel_userLevelBeanWins}）</li>
 *   <li>3.1 缺省日志实现为 {@code CFeignLogger}（{@code cFeignLogger_defaultsToCFeignLogger}）</li>
 *   <li>3.2 使用方提供日志实现时默认实现让位（{@code cFeignLogger_userLoggerBeanWins}）</li>
 * </ul>
 *
 * @since 2026/9/23
 * @version 1.0
 * @see CFeignConfiguration
 */
class CFeignConfigurationTests {

    /**
     * 对应测试用例 1.1：拦截器无条件注册为 Bean
     */
    @Test
    void cFeignInterceptor_registeredAsBean() {
        defaultRunner().run(context -> {
            // 正例：唯一不带条件注解的 @Bean，必须始终注册——它是请求拦截链路的入口
            Assertions.assertTrue(context.containsBean("cFeignInterceptor"));
            Assertions.assertInstanceOf(CFeignInterceptor.class, context.getBean(CFeignInterceptor.class));
        });
    }

    /**
     * 对应测试用例 2.1：缺省日志级别为 FULL
     */
    @Test
    void cFeignLoggerLevel_defaultsToFull() {
        defaultRunner().run(context -> {
            // 正例：无自定义 Logger.Level 时提供默认级别
            val level = context.getBean(Logger.Level.class);

            Assertions.assertEquals(Logger.Level.FULL, level);
        });
    }

    /**
     * 对应测试用例 2.2：使用方提供日志级别时默认实现让位
     */
    @Test
    void cFeignLoggerLevel_userLevelBeanWins() {
        // 使用方配置类排在前面：条件评估时其 bean 定义已存在 → 默认实现跳过
        new ApplicationContextRunner()
            .withUserConfiguration(UserLoggerLevelConfig.class, CFeignConfiguration.class)
            .withBean(CFeignClientLogConfig.class)
            .run(context -> {
                val names = context.getBeanNamesForType(Logger.Level.class);

                Assertions.assertArrayEquals(new String[] {"userLoggerLevel"}, names);
                Assertions.assertEquals(Logger.Level.BASIC, context.getBean(Logger.Level.class));
            });
    }

    /**
     * 对应测试用例 3.1：缺省日志实现为 {@code CFeignLogger}
     */
    @Test
    void cFeignLogger_defaultsToCFeignLogger() {
        defaultRunner().run(context -> {
            // 正例：无自定义 Logger 时提供项目自有实现，且配置对象已被注入
            val logger = context.getBean(Logger.class);

            Assertions.assertInstanceOf(CFeignLogger.class, logger);
        });
    }

    /**
     * 对应测试用例 3.2：使用方提供日志实现时默认实现让位
     */
    @Test
    void cFeignLogger_userLoggerBeanWins() {
        // 使用方配置类排在前面：条件评估时其 bean 定义已存在 → 默认实现跳过
        new ApplicationContextRunner()
            .withUserConfiguration(UserLoggerConfig.class, CFeignConfiguration.class)
            .withBean(CFeignClientLogConfig.class)
            .run(context -> {
                val names = context.getBeanNamesForType(Logger.class);

                Assertions.assertArrayEquals(new String[] {"userLogger"}, names);
                Assertions.assertSame(UserLoggerConfig.USER_LOGGER, context.getBean(Logger.class));
            });
    }

    /**
     * 构造仅加载 {@link CFeignConfiguration} 的上下文运行器（日志配置以普通 Bean 提供）
     *
     * @return 上下文运行器
     */
    private static ApplicationContextRunner defaultRunner() {
        return new ApplicationContextRunner()
            .withUserConfiguration(CFeignConfiguration.class)
            .withBean(CFeignClientLogConfig.class);
    }

    /**
     * 使用方自建日志级别的配置（验证条件不覆盖使用方实现）
     */
    @Configuration
    static class UserLoggerLevelConfig {

        /**
         * 使用方日志级别
         *
         * @return 日志级别
         */
        @Bean
        Logger.Level userLoggerLevel() {
            return Logger.Level.BASIC;
        }

    }

    /**
     * 使用方自建日志实现的配置（验证条件不覆盖使用方实现）
     */
    @Configuration
    static class UserLoggerConfig {

        /**
         * 使用方日志实现（实例复用以便按引用断言）
         */
        static final Logger USER_LOGGER = new Logger() {

            @Override
            protected void log(String configKey, String format, Object... args) {
                // 使用方自定义实现：本用例只验证条件装配，不校验日志输出
            }

        };

        /**
         * 使用方日志实现
         *
         * @return 日志实现
         */
        @Bean
        Logger userLogger() {
            return USER_LOGGER;
        }

    }

}
