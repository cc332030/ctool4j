package com.c332030.ctool4j.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CAuthConfig
 * </p>
 *
 * <p>认证配置属性类，绑定 {@code auth.*}。jwt 密钥为敏感信息，生产应经环境变量/配置中心注入；
 * 未配置 {@code jwtSecret} 时相关 jwt 校验不可用。</p>
 *
 * @author c332030
 * @since 2026/9/10
 */
@Data
@ConfigurationProperties("auth")
public class CAuthConfig {

    /**
     * jwt 密钥（敏感；用于 jwt 签名与校验，无默认值，需显式配置）
     */
    String jwtSecret;

}
