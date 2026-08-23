package com.c332030.ctool4j.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CPageConfig
 * </p>
 *
 * @since 2025/12/9
 * @see "doc/design/core/CPageConfig.adoc"
 * @see "doc/design/core/CPageConfigTests.adoc"
 */
@Data
@ConfigurationProperties("page")
public class CPageConfig {

    Integer defaultPageSize = 100;

}
