package com.c332030.ctool4j.spring.security.config;

import com.c332030.ctool4j.core.util.CArrUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CSpringSecurityRequestMatchersPathConfig
 * </p>
 *
 * @since 2026/1/24
 * @see "doc/design/spring/CSpringSecurityRequestMatchersPathConfig.adoc"
 */

@Data
@ConfigurationProperties("spring.security.request-matchers.path")
public class CSpringSecurityRequestMatchersPathConfig implements ICRequestMatchersConfig {

    /**
     * 允许的地址
     */
    String[] permits = CArrUtils.EMPTY_STR_ARR;

    /**
     * 禁止的地址
     */
    String[] denies = CArrUtils.EMPTY_STR_ARR;

}
