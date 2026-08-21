package com.c332030.ctool4j.minio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CMinioConfig
 * </p>
 *
 * @see doc/design/minio/CMinioConfig.adoc
 * @since 2026/7/15
 */
@Data
@ConfigurationProperties("minio")
public class CMinioConfig {

    /**
     * 访问地址
     */
    String endpoint;

    /**
     * 访问密钥 ID
     */
    String accessKey;

    /**
     * 访问密钥
     */
    String secretKey;

}
