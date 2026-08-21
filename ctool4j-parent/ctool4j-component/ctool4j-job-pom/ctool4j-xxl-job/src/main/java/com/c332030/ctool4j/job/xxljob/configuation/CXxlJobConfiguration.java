package com.c332030.ctool4j.job.xxljob.configuation;

import com.c332030.ctool4j.job.xxljob.config.CXxlJobAdminConfig;
import com.c332030.ctool4j.job.xxljob.config.CXxlJobConfig;
import com.c332030.ctool4j.job.xxljob.config.CXxlJobExecutorConfig;
import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.CustomLog;
import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * Description: CXxlJobConfiguration
 * </p>
 *
 * @see doc/design/xxljob/CXxlJobConfiguration.adoc
 * @since 2025/11/29
 */
@CustomLog
@Configuration
@ConditionalOnProperty(prefix = "xxl.job", name = "enable", havingValue = "true", matchIfMissing = true)
public class CXxlJobConfiguration {

    /**
     * 创建 xxl-job 执行器，按配置初始化 admin 与执行器参数
     *
     * @param config         执行器配置
     * @param adminConfig    admin 配置
     * @param executorConfig 执行器参数配置
     * @return 初始化完成的 XxlJobExecutor
     */
    @Bean
    @ConditionalOnMissingBean(XxlJobExecutor.class)
    public XxlJobExecutor cXxlJobExecutor(
        CXxlJobConfig config,
        CXxlJobAdminConfig adminConfig,
        CXxlJobExecutorConfig executorConfig
    ) {

        log.info(">>>>>>>>>>> xxl-job config init.");

        val executor = new XxlJobSpringExecutor();
        executor.setAccessToken(adminConfig.getAccessToken());
        executor.setAdminAddresses(adminConfig.getAddresses());
        executor.setTimeout(adminConfig.getTimeout());

        executor.setAppname(executorConfig.getAppname());
        executor.setAddress(executorConfig.getAddress());
        executor.setIp(executorConfig.getIp());
        executor.setPort(executorConfig.getPort());
        executor.setLogPath(executorConfig.getLogpath());
        executor.setLogRetentionDays(executorConfig.getLogretentiondays());

        return executor;
    }

}
