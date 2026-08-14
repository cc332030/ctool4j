package com.c332030.ctool4j.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

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
     * 请求头日志开关
     * <p>默认关闭：请求头可能含 Authorization、Cookie 等敏感信息，需要时显式开启；
     * token 等业务数据不受此开关影响，仍由业务数据区输出</p>
     */
    Boolean enableHeader = false;

    /**
     * 慢请求日志-开关
     */
    Boolean slowLogEnable = true;

    /**
     * 慢请求日志-毫秒数
     */
    Integer slowLogMillis = 3000;

    /**
     * 排除的URI列表（支持通配符 *）
     */
    Set<String> excludeUriPatterns;

}
