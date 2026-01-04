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
@ConfigurationProperties("xxl.job.admin")
public class CXxlJobAdminConfig {

    /**
     * 调度中心部署根地址 [选填]：
     * 如调度中心集群部署存在多个地址则用逗号分隔。
     * 执行器将会使用该地址进行"执行器心跳注册"和"任务结果回调"；
     * 为空则关闭自动注册；
     */
    String addresses;

    /**
     * 调度中心通讯超时时间[选填]，单位秒；默认3s；
     */
    Integer timeout;

}
