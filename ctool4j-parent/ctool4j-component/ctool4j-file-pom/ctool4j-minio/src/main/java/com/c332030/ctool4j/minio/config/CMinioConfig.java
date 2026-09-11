package com.c332030.ctool4j.minio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CMinioConfig
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CMinioConfig}（{@code @Data} + {@code @ConfigurationProperties("minio")}）承载 MinIO 连接配置：</p>
 * <ul>
 *   <li>{@code endpoint}：访问地址。</li>
 *   <li>{@code accessKey}：访问密钥 ID。</li>
 *   <li>{@code secretKey}：访问密钥。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无（配置缺失时由使用方/构建报错）。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>MinioClient 的连接参数来源。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>未做配置校验，缺配置时可能导致构建/连接失败。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>配置项</b></p>
 * <ul>
 *   <li>三个属性均无默认值，需外部配置 {@code minio.endpoint}、{@code minio.accessKey}、{@code minio.secretKey}。</li>
 * </ul>
 *
 * @since 2026/7/15
 * @version 1.0
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
