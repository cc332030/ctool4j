package com.c332030.ctool4j.spring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CSpringJacksonConfig
 * </p>
 *
 * @since 2026/2/9
 */
@Data
@ConfigurationProperties("spring.jackson")
public class CSpringJacksonConfig {

    /**
     * 是否启用 json5
     */
    Boolean json5 = false;

}
