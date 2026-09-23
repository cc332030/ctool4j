package com.c332030.ctool4j.mybatisplus.configuration;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * Description: CMybatisPlusConfigurationTests
 * </p>
 * <p>
 * 验证 {@link CMybatisPlusConfiguration#cPaginationInterceptor} 的装配契约：缺省注册分页拦截器、
 * 使用方提供同类型 Bean 时让位。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>该 {@code @Bean} 的行为是"容器装配"，故用 {@code ApplicationContextRunner} 起最小上下文，
 *   按 Bean 名与实例断言注册结果；不引入业务启动类，也不连接数据库（装配与数据源无关）。</li>
 *   <li>{@code @ConditionalOnMissingBean} 在 bean 定义注册阶段评估，故"使用方配置类"必须排在
 *   {@link CMybatisPlusConfiguration} 之前，否则条件感知不到使用方定义。</li>
 *   <li>使用方实例以静态常量持有，使"让位"可进一步按引用断言（不只是名字对）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计（方法 javadoc）：未自定义分页拦截器时默认装配 {@code PaginationInterceptor}。</li>
 *   <li>依据 {@code @ConditionalOnMissingBean} 的语义：按类型判断，已有同类型 bean 定义时跳过。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：缺省注册；使用方 Bean 覆盖的条件分支。</li>
 *   <li>未覆盖：拦截器与真实 MyBatis 执行链的协作（分页 SQL 改写需数据库，属集成测试）。</li>
 * </ul>
 * <h2>分页拦截器装配</h2>
 * <ul>
 *   <li>1.1 缺省注册 {@code PaginationInterceptor}（{@code cPaginationInterceptor_defaultsWhenMissing}）</li>
 *   <li>1.2 使用方提供时默认实现让位（{@code cPaginationInterceptor_userBeanWins}）</li>
 * </ul>
 *
 * @since 2026/9/23
 * @version 1.0
 * @see CMybatisPlusConfiguration
 */
class CMybatisPlusConfigurationTests {

    /**
     * 缺省上下文运行器：仅加载配置类本身
     */
    private static final ApplicationContextRunner DEFAULT_RUNNER =
        new ApplicationContextRunner().withUserConfiguration(CMybatisPlusConfiguration.class);

    /**
     * 对应测试用例 1.1：缺省注册 {@code PaginationInterceptor}
     */
    @Test
    void cPaginationInterceptor_defaultsWhenMissing() {
        DEFAULT_RUNNER.run(context -> {
            // 正例：无自定义拦截器时默认装配，Bean 名即工厂方法名
            Assertions.assertArrayEquals(
                new String[] {"cPaginationInterceptor"},
                context.getBeanNamesForType(PaginationInterceptor.class)
            );
            Assertions.assertInstanceOf(PaginationInterceptor.class, context.getBean(PaginationInterceptor.class));
        });
    }

    /**
     * 对应测试用例 1.2：使用方提供时默认实现让位
     */
    @Test
    void cPaginationInterceptor_userBeanWins() {
        // 使用方配置类排在前面：条件评估时其 bean 定义已存在 → 默认实现跳过
        new ApplicationContextRunner()
            .withUserConfiguration(UserPaginationInterceptorConfig.class, CMybatisPlusConfiguration.class)
            .run(context -> {
                Assertions.assertArrayEquals(
                    new String[] {"userPaginationInterceptor"},
                    context.getBeanNamesForType(PaginationInterceptor.class)
                );
                Assertions.assertSame(
                    UserPaginationInterceptorConfig.USER_INTERCEPTOR,
                    context.getBean(PaginationInterceptor.class)
                );
            });
    }

    /**
     * 使用方自建分页拦截器的配置（验证条件不覆盖使用方实现）
     */
    @Configuration
    static class UserPaginationInterceptorConfig {

        /**
         * 使用方分页拦截器（实例复用以便按引用断言）
         */
        static final PaginationInterceptor USER_INTERCEPTOR = new PaginationInterceptor();

        /**
         * 使用方分页拦截器
         *
         * @return 分页拦截器
         */
        @Bean
        PaginationInterceptor userPaginationInterceptor() {
            return USER_INTERCEPTOR;
        }

    }

}
