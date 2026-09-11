package com.c332030.ctool4j.job.xxljob.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CXxlJobExecutorLogConfig
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code logCost}：打印执行耗时，默认 true。</li>
 *   <li>{@code logCatchError}：打印捕获的错误信息，默认 true。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>未配置</td>
 *     <td>默认开启</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>控制 CXxlJobAspect 是否打印耗时/错误日志。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>默认开启，关闭时需显式配置。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>默认值</b></p>
 * <ul>
 *   <li>均默认 true。</li>
 * </ul>
 *
 * @since 2025/11/29
 * @version 1.0
 */
@Data
@ConfigurationProperties("xxl.job.executor.log")
public class CXxlJobExecutorLogConfig {

    /**
     * 打印执行耗时
     */
    Boolean logCost = true;

    /**
     * 打印捕获的错误信息
     */
    Boolean logCatchError = true;

}
