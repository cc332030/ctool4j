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
 * @since 2025/11/29
 */
@CustomLog
@Configuration
@ConditionalOnProperty(prefix = "xxl.job", name = "enable", havingValue = "true", matchIfMissing = true)
public class CXxlJobConfiguration {

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

        executor.setAppname(executorConfig.getAppname());
        executor.setAddress(executorConfig.getAddress());
        executor.setIp(executorConfig.getIp());
        executor.setPort(executorConfig.getPort());
        executor.setLogPath(executorConfig.getLogpath());
        executor.setLogRetentionDays(executorConfig.getLogretentiondays());

        return executor;
    }

}
