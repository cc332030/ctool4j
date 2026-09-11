package com.c332030.ctool4j.spring.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CSpringSecurityConfig
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CSpringSecurityConfig}：Spring Security 配置。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>配置安全规则、放行路径等</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>Web 安全配置</p>
 * <h2>不适用与边界场景</h2>
 * <p>@Configuration</p>
 * <h2>已知限制与取舍</h2>
 * <p>@Configuration</p>
 *
 * @since 2026/1/24
 * @version 1.0
 */
@Data
@ConfigurationProperties("spring.security")
public class CSpringSecurityConfig {

}
