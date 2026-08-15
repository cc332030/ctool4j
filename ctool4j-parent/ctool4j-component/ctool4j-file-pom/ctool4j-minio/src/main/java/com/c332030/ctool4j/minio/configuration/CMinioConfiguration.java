package com.c332030.ctool4j.minio.configuration;

import com.c332030.ctool4j.minio.config.CMinioConfig;
import com.c332030.ctool4j.minio.config.CMinioOkHttpConfig;
import io.minio.MinioClient;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Description: CMinioConfiguration
 * </p>
 *
 * @since 2024/12/9
 */
@Configuration
public class CMinioConfiguration {

    /**
     * 创建 Minio 使用的 OkHttpClient
     *
     * @param config OkHttp 配置
     * @return 配置了连接、读写超时的 OkHttpClient
     */
    @Bean
    public OkHttpClient cMinioOkHttpClient(CMinioOkHttpConfig config) {
        return new OkHttpClient()
            .newBuilder()
            .connectTimeout(config.getConnectTimeout(), TimeUnit.SECONDS)
            .writeTimeout(config.getWriteTimeout(), TimeUnit.SECONDS)
            .readTimeout(config.getReadTimeout(), TimeUnit.SECONDS)
            .build();
    }

    /**
     * 创建 MinioClient
     *
     * @param config     Minio 配置
     * @param httpClient OkHttpClient
     * @return 按配置构建的 MinioClient
     */
    @Bean
    public MinioClient cMinioClient(CMinioConfig config, OkHttpClient httpClient) {
        return MinioClient.builder()
                .endpoint(config.getEndpoint())
                .credentials(config.getAccessKey(), config.getSecretKey())
                .httpClient(httpClient)
                .build();
    }

}
