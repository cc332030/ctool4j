package com.c332030.ctool4j.mybatisplus.configuration;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.val;
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
 * 验证 {@link CMybatisPlusConfiguration} 三个 {@code @Bean} 的装配契约：主拦截器聚合容器内的内置拦截器
 * （含本配置类自己注册的分页与防全表拦截器）、两个内置拦截器按缺省注册且都可被使用方覆盖。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>三个 {@code @Bean} 的行为都是"容器装配"（聚合可选依赖、条件化注册），故用
 *   {@code ApplicationContextRunner} 起最小上下文；不引入业务启动类，也不连接数据库。</li>
 *   <li>主拦截器的聚合是本类的核心逻辑：{@code Collection<InnerInterceptor>} 注入容器内<b>全部</b>
 *   {@code InnerInterceptor} bean——<b>包含本配置类自己注册的分页/防全表拦截器</b>，这正是内置拦截器
 *   挂到 MyBatis-Plus 上的通路；收集时按"动态表名 → 分页 → 其余"顺序经 {@code LinkedHashSet} 忽略 null
 *   并<b>去重</b>。故主拦截器的两种输入形态都要覆盖：缺省（只有本类注册的两个）与外部额外提供。</li>
 *   <li>去重由"外部提供的动态表名拦截器同时经具名参数与集合参数进入收集"自然验证——若不去重，项数会多出一项。</li>
 *   <li>内置拦截器带 {@code @ConditionalOnBean(MybatisPlusInterceptor.class)}：该条件与本类主拦截器同源，
 *   真实装配中恒满足（主拦截器被跳过的前提是使用方已提供同类型 Bean），故只覆盖"满足"侧，
 *   不构造无法成立的"不满足"用例。</li>
 *   <li>条件在 bean 定义注册阶段评估，故"使用方配置类"必须排在 {@link CMybatisPlusConfiguration} 之前。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计（方法 javadoc）：主拦截器聚合"动态表名、分页、其他内置拦截器"三类可选依赖；
 *   分页与防全表更新删除拦截器按缺省注册。</li>
 *   <li>依据 {@code @ConditionalOnMissingBean} 的语义：按类型判断，已有同类型 bean 定义时跳过。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：缺省聚合本类注册的两个内置拦截器；外部提供时一并聚合且去重；三类默认实现各自被使用方覆盖。</li>
 *   <li>未覆盖：拦截器在真实 MyBatis 执行链中的作用（SQL 改写需数据库，属集成测试）。</li>
 * </ul>
 * <h2>MyBatis-Plus 装配</h2>
 * <ul>
 *   <li>1.1 缺省时聚合本配置类注册的两个内置拦截器（{@code cMybatisPlusInterceptor_aggregatesRegisteredInnerInterceptors}）</li>
 *   <li>1.2 外部提供的拦截器一并被聚合并去重（{@code cMybatisPlusInterceptor_includesExternallyProvidedInterceptor}）</li>
 *   <li>1.3 使用方提供主拦截器时默认实现让位（{@code cMybatisPlusInterceptor_userBeanWins}）</li>
 *   <li>2.1 缺省注册分页与防全表更新删除拦截器（{@code cInnerInterceptors_defaultsWhenMissing}）</li>
 *   <li>2.2 使用方提供内置拦截器时默认实现让位（{@code cInnerInterceptors_userBeansWin}）</li>
 * </ul>
 *
 * @since 2026/9/23
 * @version 1.0
 * @see CMybatisPlusConfiguration
 */
class CMybatisPlusConfigurationTests {

    /**
     * 对应测试用例 1.1：缺省时聚合本配置类注册的两个内置拦截器
     */
    @Test
    void cMybatisPlusInterceptor_aggregatesRegisteredInnerInterceptors() {
        new ApplicationContextRunner()
            .withUserConfiguration(CMybatisPlusConfiguration.class)
            .run(context -> {
                // 正例：本类注册的分页与防全表拦截器经集合型依赖被主拦截器聚合，
                // 没有这一步两个内置拦截器就只是游离的 Bean、不会生效
                val interceptors = context.getBean(MybatisPlusInterceptor.class).getInterceptors();

                Assertions.assertEquals(2, interceptors.size());
                Assertions.assertTrue(interceptors.stream().anyMatch(PaginationInnerInterceptor.class::isInstance));
                Assertions.assertTrue(interceptors.stream().anyMatch(BlockAttackInnerInterceptor.class::isInstance));
            });
    }

    /**
     * 对应测试用例 1.2：外部提供的拦截器一并被聚合并去重
     */
    @Test
    void cMybatisPlusInterceptor_includesExternallyProvidedInterceptor() {
        val dynamicTableName = new DynamicTableNameInnerInterceptor();

        new ApplicationContextRunner()
            .withUserConfiguration(CMybatisPlusConfiguration.class)
            .withBean(DynamicTableNameInnerInterceptor.class, () -> dynamicTableName)
            .run(context -> {
                // 正例：外部拦截器 + 本类两个内置拦截器 = 3 项。
                // 该动态表名拦截器同时经"具名参数"与"集合型依赖"进入收集，仍只算一项 —— 去重生效的证据
                val interceptors = context.getBean(MybatisPlusInterceptor.class).getInterceptors();

                Assertions.assertEquals(3, interceptors.size());
                Assertions.assertTrue(interceptors.contains(dynamicTableName));
            });
    }

    /**
     * 对应测试用例 1.3：使用方提供主拦截器时默认实现让位
     */
    @Test
    void cMybatisPlusInterceptor_userBeanWins() {
        new ApplicationContextRunner()
            .withUserConfiguration(UserMybatisPlusInterceptorConfig.class, CMybatisPlusConfiguration.class)
            .run(context -> {
                Assertions.assertArrayEquals(
                    new String[] {"userMybatisPlusInterceptor"},
                    context.getBeanNamesForType(MybatisPlusInterceptor.class)
                );
            });
    }

    /**
     * 对应测试用例 2.1：缺省注册分页与防全表更新删除拦截器
     */
    @Test
    void cInnerInterceptors_defaultsWhenMissing() {
        new ApplicationContextRunner()
            .withUserConfiguration(CMybatisPlusConfiguration.class)
            .run(context -> {
                // 正例：主拦截器存在时两个内置拦截器都按缺省注册
                Assertions.assertArrayEquals(
                    new String[] {"cPaginationInnerInterceptor"},
                    context.getBeanNamesForType(PaginationInnerInterceptor.class)
                );
                Assertions.assertArrayEquals(
                    new String[] {"cBlockAttackInnerInterceptor"},
                    context.getBeanNamesForType(BlockAttackInnerInterceptor.class)
                );
            });
    }

    /**
     * 对应测试用例 2.2：使用方提供内置拦截器时默认实现让位
     */
    @Test
    void cInnerInterceptors_userBeansWin() {
        // 使用方配置类排在前面：条件评估时其 bean 定义已存在 → 默认实现跳过
        new ApplicationContextRunner()
            .withUserConfiguration(UserInnerInterceptorsConfig.class, CMybatisPlusConfiguration.class)
            .run(context -> {
                Assertions.assertArrayEquals(
                    new String[] {"userPaginationInnerInterceptor"},
                    context.getBeanNamesForType(PaginationInnerInterceptor.class)
                );
                Assertions.assertArrayEquals(
                    new String[] {"userBlockAttackInnerInterceptor"},
                    context.getBeanNamesForType(BlockAttackInnerInterceptor.class)
                );
            });
    }

    /**
     * 使用方自建主拦截器的配置（验证条件不覆盖使用方实现）
     */
    @Configuration
    static class UserMybatisPlusInterceptorConfig {

        /**
         * 使用方主拦截器
         *
         * @return 主拦截器
         */
        @Bean
        MybatisPlusInterceptor userMybatisPlusInterceptor() {
            return new MybatisPlusInterceptor();
        }

    }

    /**
     * 使用方自建两个内置拦截器的配置（验证条件不覆盖使用方实现）
     */
    @Configuration
    static class UserInnerInterceptorsConfig {

        /**
         * 使用方分页拦截器
         *
         * @return 分页拦截器
         */
        @Bean
        PaginationInnerInterceptor userPaginationInnerInterceptor() {
            return new PaginationInnerInterceptor();
        }

        /**
         * 使用方防全表更新删除拦截器
         *
         * @return 防全表更新删除拦截器
         */
        @Bean
        BlockAttackInnerInterceptor userBlockAttackInnerInterceptor() {
            return new BlockAttackInnerInterceptor();
        }

    }

}
