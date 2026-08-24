package com.c332030.ctool4j.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

/**
 * <p>
 * Description: CRequestLogConfig
 * </p>
 *
 * @since 2025/9/29
 * @see "doc/design/web/CRequestLogConfig.adoc"
 */
@Data
@ConfigurationProperties("logging.request-log")
public class CRequestLogConfig extends CRequestLogBaseConfig {

    /**
     * 排除的URI列表（支持通配符 *）
     */
    Set<String> excludeUriPatterns;

}
