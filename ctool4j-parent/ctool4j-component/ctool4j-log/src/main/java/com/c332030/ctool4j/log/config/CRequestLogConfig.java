package com.c332030.ctool4j.log.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CRequestLogConfig
 * </p>
 *
 * @since 2025/9/29
 */
@Data
@ConfigurationProperties("logging.request-log")
public class CRequestLogConfig {

    Boolean enable = false;

}
