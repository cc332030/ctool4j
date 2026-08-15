package com.c332030.ctool4j.spring.security.configuration;

import com.c332030.ctool4j.spring.security.config.CSpringSecurityConfig;
import com.c332030.ctool4j.spring.security.config.CSpringSecurityRequestMatchersPathConfig;
import com.c332030.ctool4j.spring.security.core.CAccessDeniedHandler;
import com.c332030.ctool4j.spring.security.core.CAuthenticationEntryPoint;
import com.c332030.ctool4j.spring.security.core.CSessionInformationExpiredStrategy;
import com.c332030.ctool4j.spring.security.filter.CAbstractJwtFilter;
import com.c332030.ctool4j.spring.security.service.impl.CEmptyUserDetailService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

/**
 * <p>
 * Description: CSecurityConfiguration
 * </p>
 *
 * @since 2026/1/22
 */
@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class CSecurityConfiguration {

    CSpringSecurityConfig config;

    /**
     * 创建密码编码器
     *
     * @return BCrypt 密码编码器
     */
    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder cPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 创建认证管理器
     *
     * @param authConfig 认证配置
     * @return 认证管理器
     */
    @Bean
    @SneakyThrows
    @ConditionalOnMissingBean(AuthenticationManager.class)
    public AuthenticationManager cAuthenticationManager(AuthenticationConfiguration authConfig) {
        return authConfig.getAuthenticationManager();
    }

    /**
     * 创建认证入口
     *
     * @return 认证入口
     */
    @Bean
    @ConditionalOnMissingBean(AuthenticationEntryPoint.class)
    public AuthenticationEntryPoint cAuthenticationEntryPoint() {
        return new CAuthenticationEntryPoint();
    }

    /**
     * 创建访问拒绝处理器
     *
     * @return 访问拒绝处理器
     */
    @Bean
    @ConditionalOnMissingBean(AccessDeniedHandler.class)
    public AccessDeniedHandler cAccessDeniedHandler() {
        return new CAccessDeniedHandler();
    }

    /**
     * 创建会话过期策略
     *
     * @return 会话过期策略
     */
    @Bean
    @ConditionalOnMissingBean(SessionInformationExpiredStrategy.class)
    public SessionInformationExpiredStrategy cSessionInformationExpiredStrategy() {
        return new CSessionInformationExpiredStrategy();
    }

    /**
     * 创建空用户详情服务，供无自定义实现时兜底
     *
     * @return 空用户详情服务
     */
    @Bean
    @ConditionalOnMissingBean(UserDetailsService.class)
    public UserDetailsService cUserDetailsService() {
        return new CEmptyUserDetailService();
    }

    /**
     * 创建安全过滤器链，配置认证、授权、异常处理与会话管理
     *
     * @param http                            HttpSecurity
     * @param authenticationEntryPoint        认证入口
     * @param accessDeniedHandler             访问拒绝处理器
     * @param sessionInformationExpiredStrategy 会话过期策略
     * @param requestMatchersPathConfig       请求匹配路径配置
     * @param jwtFilter                       JWT 过滤器
     * @return 安全过滤器链
     * @throws Exception 构建过滤器链失败时抛出
     */
    @Bean
    // 安全过滤链绑定了 permit/deny 等安全路径配置（CSpringSecurityRequestMatchersPathConfig），
    // 该配置可来自配置中心（如 Nacos/Spring Cloud Config），使用 @RefreshScope 使安全规则变更即时生效，无需重启应用
    @RefreshScope
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain cFilterChain(
        HttpSecurity http,
        AuthenticationEntryPoint authenticationEntryPoint,
        AccessDeniedHandler accessDeniedHandler,
        SessionInformationExpiredStrategy sessionInformationExpiredStrategy,
        CSpringSecurityRequestMatchersPathConfig requestMatchersPathConfig,
        CAbstractJwtFilter jwtFilter
    ) throws Exception {

        return http
            .csrf(AbstractHttpConfigurer::disable)
            // 禁用自带的表单登录 /login
            .formLogin().disable()
            // 禁止 anonymous
            .anonymous().disable()
            // 启用“记住我”功能的。允许用户在关闭浏览器后，仍然保持登录状态，直到他们主动注销或超出设定的过期时间。
            .rememberMe(Customizer.withDefaults())
            // 验证
            .addFilterAfter(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            // 关键：关闭默认的 401/403 页面跳转，交由全局异常处理器处理
            .exceptionHandling( ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            // 登录过期处理
            .sessionManagement(session -> session
                .maximumSessions(Integer.MAX_VALUE)
                .expiredSessionStrategy(sessionInformationExpiredStrategy)
            )
            // 开启授权保护
            .authorizeHttpRequests(authorize -> authorize

                // 不需要认证的地址有哪些
                .antMatchers(requestMatchersPathConfig.getPermits())
                .permitAll()

                // 禁止的地址有哪些
                .antMatchers(requestMatchersPathConfig.getDenies())
                .denyAll()

                // 对所有请求开启授权保护
                .anyRequest()
                // 已认证的请求会被自动授权
                .authenticated()
            )
            .build();
    }

}
