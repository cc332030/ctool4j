package com.c332030.ctool4j.feign.config;

import com.c332030.ctool4j.core.util.CMap;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * <p>
 * Description: CFeignConfig
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CFeignConfig}（{@code @ConfigurationProperties(value="feign", ignoreInvalidFields=true)}）为 Feign 全局配置：</p>
 * <ul>
 *   <li>{@code client}：{@code Map&lt;String, ClientConfig&gt;}，客户端信息（key 为客户端名，value 含 {@code url} 地址）。</li>
 * </ul>
 * <p>内部 {@code ClientConfig} 为客户端配置，含 {@code url} 字段。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>前缀 {@code feign}，忽略格式不一致字段。</li>
 *   <li>{@code client} 默认 {@code CMap.of()}（空不可变 Map）。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>未配置时为空 client Map。</li>
 * </ul>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>配置多 Feign 客户端地址（按名）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>仅配置载体；具体客户端装配由 Spring 完成。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>仅解析 {@code url} 字段，其他客户端配置不在此承载。</li>
 * </ul>
 *
 * @since 2025/9/21
 * @version 1.0
 */
@Data
@ConfigurationProperties(
    value = "feign",
    // 忽略格式不一样的字段
    ignoreInvalidFields = true
)
public class CFeignConfig {

    /**
     * 客户端信息
     */
    Map<String, ClientConfig> client = CMap.of();

}

/**
 * 客户端配置
 */
@Data
class ClientConfig {

    /**
     * 客户端地址
     */
    String url;

}
