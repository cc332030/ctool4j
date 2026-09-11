package com.c332030.ctool4j.spring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CSpringApplicationConfig
 * </p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>绑定应用 group/name 等配置，供各工具读取前缀</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>未配置时取空</p>
 * <h2>适用范围</h2>
 * <p>应用标识配置</p>
 * <h2>不适用与边界场景</h2>
 * <p>供 CRedisUtils 等使用</p>
 * <h2>已知限制与取舍</h2>
 * <p>供 CRedisUtils 等使用</p>
 *
 * @since 2025/11/10
 * @version 1.0
 */
@Data
@ConfigurationProperties("spring.application")
public class CSpringApplicationConfig {

    /**
     * 分组-自定义属性
     */
    String group;

    /**
     * 应用名称
     */
    String name;

}
