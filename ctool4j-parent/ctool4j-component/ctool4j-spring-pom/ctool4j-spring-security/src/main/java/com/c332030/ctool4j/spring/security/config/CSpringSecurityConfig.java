package com.c332030.ctool4j.spring.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * <p>
 * Description: CSpringSecurityConfig
 * </p>
 *
 * @since 2026/1/24
 */
@Data
@ConfigurationProperties("spring.security")
public class CSpringSecurityConfig {

    List<String> permitMatchers;

}
