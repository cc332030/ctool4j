package com.c332030.ctool4j.doc.openapi2.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CDocOpenApi2Config
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CDocOpenApi2Config}（{@code @Data} + {@code @ConfigurationProperties("c-doc.openapi2")}）承载 OpenAPI2 文档相关配置：</p>
 * <ul>
 *   <li>{@code pathMapping}：路径映射，一般用于 nginx 反向代理后 knife4j 检测不到路径的情况，默认 {@code "/"}。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>pathMapping 未配置</td>
 *     <td>默认 {@code "/"}</td>
 *   </tr>
 *   <tr>
 *     <td>pathMapping 显式置 null</td>
 *     <td>保持 null（由调用方处理）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>反向代理场景下 OpenAPI2 文档的路径映射配置。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>仅承载单一路径映射属性，未做额外校验。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>pathMapping</b></p>
 * <ul>
 *   <li>默认值为 {@code "/"}。</li>
 *   <li>可通过配置文件 {@code c-doc.openapi2.pathMapping} 覆盖。</li>
 * </ul>
 *
 * @since 2026/1/6
 * @version 1.0
 */
@Data
@ConfigurationProperties("c-doc.openapi2")
public class CDocOpenApi2Config {

    /**
     * 路径映射，一般是 nginx 做了反向代理，knife4j 检测不到
     */
    String pathMapping = "/";

}
