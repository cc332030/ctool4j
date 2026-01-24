package com.c332030.ctool4j.spring.security.config;

import com.c332030.ctool4j.core.util.CList;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * <p>
 * Description: CSpringSecurityRequestMatchersPathConfig
 * </p>
 *
 * @since 2026/1/24
 */

@Data
@ConfigurationProperties("spring.security.request-matchers.path")
public class CSpringSecurityRequestMatchersPathConfig {

    /**
     * 允许的地址
     */
    List<String> permits = CList.of();

    /**
     * 禁止的地址
     */
    List<String> denies = CList.of();

}
