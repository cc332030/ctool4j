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
 * <h2>能力目录</h2>
 * <p>{@code CMinioConfiguration}（{@code @Configuration}）注册 MinIO 相关 Bean：</p>
 * <ul>
 *   <li>{@code cMinioOkHttpClient}：按 {@code CMinioOkHttpConfig} 构建 OkHttpClient（配置连接/写/读超时）。</li>
 *   <li>{@code cMinioClient}：按 {@code CMinioConfig} + OkHttpClient 构建 {@code MinioClient}。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无（配置缺失时构建可能失败）。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>注入 MinIO 客户端与底层 OkHttpClient。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>依赖 {@code CMinioConfig}/{@code CMinioOkHttpConfig} 配置属性。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>Bean 构建</b></p>
 * <ul>
 *   <li>OkHttpClient 通过 builder 设置 connect/write/read 超时。</li>
 *   <li>MinioClient 用 builder 设置 endpoint、credentials、httpClient。</li>
 * </ul>
 *
 * @since 2024/12/9
 * @version 1.0
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
