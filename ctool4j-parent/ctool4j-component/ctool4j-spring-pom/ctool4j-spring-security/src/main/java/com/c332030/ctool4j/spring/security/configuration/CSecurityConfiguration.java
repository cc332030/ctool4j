package com.c332030.ctool4j.spring.security.configuration;

import com.c332030.ctool4j.spring.security.config.CSpringSecurityConfig;
import com.c332030.ctool4j.spring.security.config.CSpringSecurityRequestMatchersPathConfig;
import com.c332030.ctool4j.spring.security.core.CAccessDeniedHandler;
import com.c332030.ctool4j.spring.security.core.CAuthenticationEntryPoint;
import com.c332030.ctool4j.spring.security.core.CSessionInformationExpiredStrategy;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
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

    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @SneakyThrows
    @ConditionalOnMissingBean(AuthenticationManager.class)
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationEntryPoint.class)
    public AuthenticationEntryPoint cAuthenticationEntryPoint() {
        return new CAuthenticationEntryPoint();
    }

    @Bean
    @ConditionalOnMissingBean(AccessDeniedHandler.class)
    public AccessDeniedHandler cAccessDeniedHandler() {
        return new CAccessDeniedHandler();
    }

    @Bean
    @ConditionalOnMissingBean(SessionInformationExpiredStrategy.class)
    public SessionInformationExpiredStrategy cSessionInformationExpiredStrategy() {
        return new CSessionInformationExpiredStrategy();
    }

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain filterChain(
        HttpSecurity http,
        AuthenticationEntryPoint authenticationEntryPoint,
        AccessDeniedHandler accessDeniedHandler,
        SessionInformationExpiredStrategy sessionInformationExpiredStrategy,
        CSpringSecurityRequestMatchersPathConfig requestMatchersPathConfig
    ) throws Exception {

        return http
            .csrf(AbstractHttpConfigurer::disable)
            // 禁用自带的表单登录 /login
            .formLogin().disable()
            // 禁止 anonymous
            .anonymous().disable()
            // 启用“记住我”功能的。允许用户在关闭浏览器后，仍然保持登录状态，直到他们主动注销或超出设定的过期时间。
            .rememberMe(Customizer.withDefaults())
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
