package com.c332030.ctool4j.job.xxljob.config;

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
@ConfigurationProperties("xxl.job")
public class CXxlJobConfig {

    /**
     * 是否开启，比如本地不开启，默认开启
     */
    Boolean enable;

    /**
     * 调度中心通讯TOKEN [选填]：非空时启用；
     */
    String accessToken;

}
