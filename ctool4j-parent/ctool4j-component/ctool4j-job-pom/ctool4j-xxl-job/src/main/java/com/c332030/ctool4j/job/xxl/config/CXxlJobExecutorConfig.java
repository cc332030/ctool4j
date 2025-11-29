package com.c332030.ctool4j.job.xxl.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CXxlJobExecutorConfig
 * </p>
 *
 * @since 2025/11/29
 */
@Data
@ConfigurationProperties("xxl.job.executor.log")
public class CXxlJobExecutorConfig {

    /**
     * 打印捕获的错误信息
     */
    Boolean logCatchError = true;

}
