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
 * <h2>能力目录</h2>
 * <p>{@code CXxlJobConfiguration}（{@code @Configuration} + {@code @ConditionalOnProperty(xxl.job.enable=true, matchIfMissing=true)}）注册 xxl-job 执行器 Bean。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>xxl.job.enable 未配置</td>
 *     <td>matchIfMissing 默认开启</td>
 *   </tr>
 *   <tr>
 *     <td>已有 XxlJobExecutor Bean</td>
 *     <td>跳过</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>xxl-job 执行器的自动装配。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>依赖 admin/executor 配置。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>执行器 Bean</b></p>
 * <ul>
 *   <li>{@code @ConditionalOnMissingBean(XxlJobExecutor.class)}。</li>
 *   <li>用 {@code XxlJobSpringExecutor} 按 admin/executor 配置初始化（accessToken、addresses、timeout、appname、address、ip、port、logPath、logRetentionDays）。</li>
 * </ul>
 *
 * @since 2025/11/29
 * @version 1.0
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
