package com.c332030.ctool4j.job.xxljob.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CXxlJobConfig
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CXxlJobConfig}（{@code @Data} + {@code @ConfigurationProperties("xxl.job")}）承载 xxl-job 总开关配置：</p>
 * <ul>
 *   <li>{@code enable}：是否开启，默认开启（本地可关闭）。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>enable 未配置</td>
 *     <td>matchIfMissing 默认开启</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>控制 xxl-job 是否启用。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>默认开启，需关闭时显式配置。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>enable</b></p>
 * <ul>
 *   <li>控制 xxl-job 执行器是否注册（{@code @ConditionalOnProperty} 配合使用）。</li>
 * </ul>
 *
 * @since 2025/11/29
 * @version 1.0
 */
@Data
@ConfigurationProperties("xxl.job")
public class CXxlJobConfig {

    /**
     * 是否开启，比如本地不开启，默认开启
     */
    Boolean enable;

}
