package com.c332030.ctool4j.spring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CSpringJacksonConfig
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CSpringJacksonConfig}：Jackson 配置。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>配置 Jackson 序列化/反序列化行为</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>默认配置</p>
 * <h2>适用范围</h2>
 * <p>JSON 行为配置</p>
 * <h2>不适用与边界场景</h2>
 * <p>供消息转换器使用</p>
 * <h2>已知限制与取舍</h2>
 * <p>供消息转换器使用</p>
 *
 * @since 2026/2/9
 * @version 1.0
 */
@Data
@ConfigurationProperties("spring.jackson")
public class CSpringJacksonConfig {

    /**
     * 是否启用 json5
     */
    Boolean json5 = false;

}
