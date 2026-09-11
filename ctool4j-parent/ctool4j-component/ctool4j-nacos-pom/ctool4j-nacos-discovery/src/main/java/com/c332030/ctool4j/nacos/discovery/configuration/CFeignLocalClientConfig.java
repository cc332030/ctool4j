package com.c332030.ctool4j.nacos.discovery.configuration;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Map;

/**
 * <p>
 * Description: CFeignLocalClientConfig
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CFeignLocalClientConfig}（{@code @Data} + {@code @ConfigurationProperties("feign.client.local-instance")}）承载本地客户端实例注册配置：</p>
 * <ul>
 *   <li>{@code urls}：{@code Map&lt;String, String&gt;}，服务名 → ip:port 映射，默认空 Map。</li>
 *   <li>通过 {@code @ConditionalOnProperty}（{@code feign.client.local-instance.enabled=true}）启用。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>urls 未配置</td>
 *     <td>默认空 Map</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>将本地 Feign 客户端实例注册到 Nacos 的配置来源。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>依赖 {@code @ConditionalOnProperty} 启用，未启用时配置不生效。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>urls</b></p>
 * <ul>
 *   <li>键为服务名，值为 {@code ip:port} 字符串。</li>
 *   <li>默认 {@code Collections.emptyMap()}。</li>
 * </ul>
 *
 * @since 2024/12/9
 * @version 1.0
 */
@Data
@ConfigurationProperties("feign.client.local-instance")
@ConditionalOnProperty(prefix = "feign.client.local-instance", value = "enabled", havingValue = "true")
public class CFeignLocalClientConfig {

    Map<String, String> urls = Collections.emptyMap();

}
