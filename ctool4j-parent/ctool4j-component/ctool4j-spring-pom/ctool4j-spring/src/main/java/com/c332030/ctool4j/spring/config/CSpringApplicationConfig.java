package com.c332030.ctool4j.spring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CSpringApplicationConfig
 * </p>
 *
 * @since 2025/11/10
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
