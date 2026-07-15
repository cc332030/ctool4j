package com.c332030.ctool4j.minio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CMinioOkHttpConfig
 * </p>
 *
 * @since 2026/7/15
 */
@Data
@ConfigurationProperties("minio.okhttp")
public class CMinioOkHttpConfig {

    /**
     * 连接超时（秒）
     */
    Integer connectTimeout = 1;

    /**
     * 写超时（秒）
     */
    Integer writeTimeout = 3;

    /**
     * 读超时（秒）
     */
    Integer readTimeout = 3;

}
