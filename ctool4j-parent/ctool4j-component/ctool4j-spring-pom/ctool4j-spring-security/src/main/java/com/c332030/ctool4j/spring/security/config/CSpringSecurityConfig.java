package com.c332030.ctool4j.spring.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CSpringSecurityConfig
 * </p>
 *
 * @since 2026/1/24
 * @see "doc/design/spring/CSpringSecurityConfig.adoc"
 */
@Data
@ConfigurationProperties("spring.security")
public class CSpringSecurityConfig {

}
