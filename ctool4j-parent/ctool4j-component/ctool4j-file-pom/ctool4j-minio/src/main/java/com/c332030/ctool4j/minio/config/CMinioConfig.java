package com.c332030.ctool4j.minio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CMinioConfig
 * </p>
 *
 * @since 2026/7/15
 */
@Data
@ConfigurationProperties("minio")
public class CMinioConfig {

    /**
     * 访问地址
     */
    String endpoint;

    String accessKey;

    String secretKey;

}
