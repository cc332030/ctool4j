package com.c332030.ctool4j.log.config;

import com.c332030.ctool4j.log.enums.CRequestLogTypeEnum;
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
     * 请求日志开关
     */
    CRequestLogTypeEnum type = CRequestLogTypeEnum.ADVICE;

    /**
     * 慢请求日志-开关
     */
    Boolean slowLogEnable = false;

    /**
     * 慢请求日志-毫秒数
     */
    Integer slowLogMillis = 10000;

}
