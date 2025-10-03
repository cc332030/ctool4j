package com.c332030.ctool4j.log.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CRequestLogConfig
 * </p>
 *
 * @since 2025/9/29
 */
@Data
@ConfigurationProperties("logging.request-log")
public class CRequestLogConfig {

    /**
     * 请求日志开关
     */
    Boolean enable = false;

    /**
     * 慢请求毫秒数-打印出来
     */
    Integer slowMillis = 10000;

}
