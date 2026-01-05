package com.c332030.ctool4j.feign.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CFeignClientLogConfig
 * </p>
 *
 * @since 2025/9/21
 */
@Data
@ConfigurationProperties("feign.client.log")
public class CFeignClientLogConfig {

    /**
     * 日志开关
     */
    Boolean enable = false;

    /**
     * 请求头日志开关
     */
    Boolean enableHeader = false;

    /**
     * 耗时日志开关
     */
    Boolean enableCost = true;

}
