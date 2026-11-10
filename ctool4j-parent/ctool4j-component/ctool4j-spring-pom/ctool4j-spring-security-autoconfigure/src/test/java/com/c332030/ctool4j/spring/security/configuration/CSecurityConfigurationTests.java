package com.c332030.ctool4j.spring.security.configuration;

import com.c332030.ctool4j.spring.security.config.CSpringSecurityConfig;
import com.c332030.ctool4j.spring.security.config.CSpringSecurityRequestMatchersPathConfig;
import com.c332030.ctool4j.spring.security.core.CAccessDeniedHandler;
import com.c332030.ctool4j.spring.security.core.CAuthenticationEntryPoint;
import com.c332030.ctool4j.spring.security.core.CSessionInformationExpiredStrategy;
import com.c332030.ctool4j.spring.security.service.impl.CEmptyUserDetailService;
import com.c332030.ctool4j.web.filter.CAbstractWebAuthFilter;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;


/**
 * <p>
 * Description: CSecurityConfigurationTests
 * </p>
 * <p>
 * 验证 {@link CSecurityConfiguration} 七个 {@code @Bean} 的装配契约：六个安全组件按缺省注册且可被使用方覆盖，
 * 安全过滤器链在缺省装配下成立。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>本类带 {@code @EnableWebSecurity} 且 {@code cFilterChain} 依赖 {@code HttpSecurity}，故用
 *   {@code WebApplicationContextRunner}（servlet 环境）起上下文；用 {@code ApplicationContextRunner}
 *   无法满足安全链的装配前提。</li>
 *   <li>{@code cFilterChain} 标了 {@code @RefreshScope}（安全规则随配置中心刷新），而 {@code refresh}
 *   是自定义作用域、必须由 {@code RefreshAutoConfiguration} 注册——不引入该自动配置会在创建该 Bean 时抛
 *   "No Scope registered for scope name 'refresh'"，故测试里显式装配它。</li>
 *   <li>{@code cFilterChain} 按 {@code CAbstractWebAuthFilter} 注入认证过滤器（用该确定类型避免与 Spring Boot
 *   内置过滤器产生注入歧义），故用例提供它的测试桩子类。</li>
 *   <li>密码编码器不止断言类型，还用"编码后能校验原值、不能校验别的值"做行为断言：BCrypt 编码器返回了但
 *   算法退化成明文/无盐时，只有行为断言能捕获。</li>
 *   <li>条件在 bean 定义注册阶段评估，故"使用方配置类"必须排在 {@link CSecurityConfiguration} 之前。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计（方法 javadoc）：六个组件按缺省注册（{@code @ConditionalOnMissingBean} 可被覆盖）；
 *   安全过滤器链配置认证、授权、异常处理与会话管理。</li>
 *   <li>依据 {@code CAbstractWebAuthFilter} 的 javadoc：它是认证过滤器的类型契约，按该类型注入以避免
 *   {@code OncePerRequestFilter} 的注入歧义。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：密码编码器的算法行为与覆盖分支；四个安全处理组件的缺省实现类型；认证管理器注册；
 *   安全过滤器链缺省装配成立。</li>
 *   <li>未覆盖：过滤链各配置项（permit/deny 路径、会话并发上限等）的实际拦截效果（需发真实请求，
 *   属端到端测试）；{@code @RefreshScope} 刷新后规则是否即时生效（需配置中心）。</li>
 * </ul>
 * <h2>Security 装配</h2>
 * <ul>
 *   <li>1.1 缺省密码编码器为 BCrypt 且校验行为正确（{@code cPasswordEncoder_defaultsToBCrypt}）</li>
 *   <li>1.2 使用方提供密码编码器时默认实现让位（{@code cPasswordEncoder_userBeanWins}）</li>
 *   <li>2.1 四个安全处理组件按缺省注册为项目自有实现（{@code cSecurityComponents_defaultsRegistered}）</li>
 *   <li>3.1 缺省注册认证管理器（{@code cAuthenticationManager_registered}）</li>
 *   <li>4.1 安全过滤器链在缺省装配下成立（{@code cFilterChain_registered}）</li>
 * </ul>
 *
 * @since 2026/9/23
 * @version 1.0
 * @see CSecurityConfiguration
 */
class CSecurityConfigurationTests {

    /**
     * 对应测试用例 1.1：缺省密码编码器为 BCrypt 且校验行为正确
     */
    @Test
    void cPasswordEncoder_defaultsToBCrypt() {
        runner(CSecurityConfiguration.class).run(context -> {
            // 正例：缺省实现必须是 BCrypt（安全默认），且真能编码与校验
            val encoder = context.getBean(PasswordEncoder.class);

            Assertions.assertInstanceOf(BCryptPasswordEncoder.class, encoder);

            val encoded = encoder.encode("secret-password");
            Assertions.assertTrue(encoder.matches("secret-password", encoded));
            Assertions.assertFalse(encoder.matches("other-password", encoded));
        });
    }

    /**
     * 对应测试用例 1.2：使用方提供密码编码器时默认实现让位
     */
    @Test
    void cPasswordEncoder_userBeanWins() {
        // 使用方配置类排在前面：条件评估时其 bean 定义已存在 → 默认实现跳过
        runner(UserPasswordEncoderConfig.class, CSecurityConfiguration.class).run(context -> {
            Assertions.assertArrayEquals(
                new String[] {"userPasswordEncoder"},
                context.getBeanNamesForType(PasswordEncoder.class)
            );
            Assertions.assertSame(
                UserPasswordEncoderConfig.USER_ENCODER,
                context.getBean(PasswordEncoder.class)
            );
        });
    }

    /**
     * 对应测试用例 2.1：四个安全处理组件按缺省注册为项目自有实现
     */
    @Test
    void cSecurityComponents_defaultsRegistered() {
        runner(CSecurityConfiguration.class).run(context -> {
            // 正例：认证入口/访问拒绝/会话过期/用户详情四个缺省实现按项目类型装配
            Assertions.assertInstanceOf(
                CAuthenticationEntryPoint.class, context.getBean(AuthenticationEntryPoint.class));
            Assertions.assertInstanceOf(
                CAccessDeniedHandler.class, context.getBean(AccessDeniedHandler.class));
            Assertions.assertInstanceOf(
                CSessionInformationExpiredStrategy.class, context.getBean(SessionInformationExpiredStrategy.class));
            Assertions.assertInstanceOf(
                CEmptyUserDetailService.class, context.getBean(UserDetailsService.class));
        });
    }

    /**
     * 对应测试用例 3.1：缺省注册认证管理器
     */
    @Test
    void cAuthenticationManager_registered() {
        runner(CSecurityConfiguration.class).run(context -> {
            // 正例：由 @EnableWebSecurity 的认证配置派生，不能为空（安全链与登录流程都依赖它）
            Assertions.assertNotEquals(0, context.getBeanNamesForType(AuthenticationManager.class).length);
            Assertions.assertNotNull(context.getBean(AuthenticationManager.class));
        });
    }

    /**
     * 对应测试用例 4.1：安全过滤器链在缺省装配下成立
     */
    @Test
    void cFilterChain_registered() {
        runner(CSecurityConfiguration.class).run(context -> {
            // 正例：缺省过滤器链装配成立，且确实带上了过滤器（空链等于没有安全防护）
            val chain = context.getBean(SecurityFilterChain.class);

            Assertions.assertNotNull(chain);
            Assertions.assertFalse(chain.getFilters().isEmpty());
        });
    }

    /**
     * 构造加载 {@link CSecurityConfiguration} 与给定用户配置的 Web 上下文运行器
     * <p>用户配置在前，保证 {@code @ConditionalOnMissingBean} 的条件评估能看到使用方定义；
     * 并装配 refresh 作用域（{@code cFilterChain} 标了 {@code @RefreshScope}）与认证过滤器测试桩</p>
     *
     * @param userConfigurations 用户配置类（顺序即注册顺序）
     * @return Web 上下文运行器
     */
    private static WebApplicationContextRunner runner(Class<?>... userConfigurations) {
        return new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RefreshAutoConfiguration.class))
            .withUserConfiguration(userConfigurations)
            .withBean(CSpringSecurityConfig.class)
            .withBean(CSpringSecurityRequestMatchersPathConfig.class, CSpringSecurityRequestMatchersPathConfig::new)
            .withBean(CAbstractWebAuthFilter.class, TestWebAuthFilter::new);
    }

    /**
     * 使用方自建密码编码器的配置（验证条件不覆盖使用方实现）
     */
    @Configuration
    static class UserPasswordEncoderConfig {

        /**
         * 使用方密码编码器（实例复用以便按引用断言）
         */
        static final PasswordEncoder USER_ENCODER = new BCryptPasswordEncoder();

        /**
         * 使用方密码编码器
         *
         * @return 密码编码器
         */
        @Bean
        PasswordEncoder userPasswordEncoder() {
            return USER_ENCODER;
        }

    }

    /**
     * 认证过滤器测试桩：{@link CAbstractWebAuthFilter} 只承载类型契约，
     * 抽象层版本默认放行（本用例不校验过滤行为），故无需覆写
     */
    static class TestWebAuthFilter extends CAbstractWebAuthFilter {

        // 抽象层版本默认放行；本用例只验证过滤器链装配，故无需覆写任何方法

    }

}
