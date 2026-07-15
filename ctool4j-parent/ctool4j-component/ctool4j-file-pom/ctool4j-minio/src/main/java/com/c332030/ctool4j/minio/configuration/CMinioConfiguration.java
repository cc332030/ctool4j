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
 * Description: HydrusConfiguration
 * </p>
 *
 * @since 2024/12/9
 */
@Configuration
public class CMinioConfiguration {

    @Bean
    public OkHttpClient cMinioOkHttpClient(CMinioOkHttpConfig config) {
        return new OkHttpClient()
            .newBuilder()
            .connectTimeout(config.getConnectTimeout(), TimeUnit.SECONDS)
            .writeTimeout(config.getWriteTimeout(), TimeUnit.SECONDS)
            .readTimeout(config.getReadTimeout(), TimeUnit.SECONDS)
            .build();
    }

    @Bean
    public MinioClient cMinioClient(CMinioConfig config, OkHttpClient httpClient) {
        return MinioClient.builder()
                .endpoint(config.getEndpoint())
                .credentials(config.getAccessKey(), config.getSecretKey())
                .httpClient(httpClient)
                .build();
    }

}
