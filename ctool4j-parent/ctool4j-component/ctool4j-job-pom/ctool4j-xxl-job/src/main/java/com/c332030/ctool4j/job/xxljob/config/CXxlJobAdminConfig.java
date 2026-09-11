package com.c332030.ctool4j.job.xxljob.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CXxlJobAdminConfig
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CXxlJobAdminConfig}（{@code @Data} + {@code @ConfigurationProperties("xxl.job.admin")}）承载 xxl-job 调度中心配置：</p>
 * <ul>
 *   <li>{@code addresses}：调度中心根地址（多个用逗号分隔，为空关闭自动注册）。</li>
 *   <li>{@code accessToken}：通讯 TOKEN（非空时启用）。</li>
 *   <li>{@code timeout}：通讯超时（秒），默认 3。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>未配置 timeout</td>
 *     <td>默认 3 秒</td>
 *   </tr>
 *   <tr>
 *     <td>addresses 为空</td>
 *     <td>关闭自动注册</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>xxl-job 执行器连接调度中心的参数来源。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>未做校验，配置缺失时由 xxl-job 框架行为决定。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>配置项</b></p>
 * <ul>
 *   <li>addresses/accessToken 为空时关闭对应能力。</li>
 *   <li>timeout 默认 3 秒。</li>
 * </ul>
 *
 * @since 2025/11/29
 * @version 1.0
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
     * 调度中心通讯TOKEN [选填]：非空时启用
     */
    String accessToken;

    /**
     * 调度中心通讯超时时间[选填]，单位秒；未配置时使用字段默认值
     */
    Integer timeout = 3;

}
