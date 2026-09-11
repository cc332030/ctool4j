package com.c332030.ctool4j.minio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CMinioOkHttpConfig
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CMinioOkHttpConfig}（{@code @Data} + {@code @ConfigurationProperties("minio.okhttp")}）承载 MinIO 客户端 OkHttp 超时配置：</p>
 * <ul>
 *   <li>{@code connectTimeout}：连接超时（秒），默认 1。</li>
 *   <li>{@code writeTimeout}：写超时（秒），默认 3。</li>
 *   <li>{@code readTimeout}：读超时（秒），默认 3。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>未配置</td>
 *     <td>使用默认超时（1/3/3 秒）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>构建 MinIO 客户端所用 OkHttpClient 的超时参数。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>默认超时较短，大文件/慢网络场景需按需调大。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>默认值</b></p>
 * <ul>
 *   <li>连接 1s、写 3s、读 3s，可通过 {@code minio.okhttp.*} 覆盖。</li>
 * </ul>
 *
 * @since 2026/7/15
 * @version 1.0
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
