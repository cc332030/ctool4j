package com.c332030.ctool4j.job.xxljob.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CXxlJobExecutorConfig
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CXxlJobExecutorConfig}（{@code @Data} + {@code @ConfigurationProperties("xxl.job.executor")}）承载 xxl-job 执行器参数：</p>
 * <ul>
 *   <li>{@code appname}：执行器 AppName（为空关闭自动注册）。</li>
 *   <li>{@code address}：执行器注册地址（优先使用，为空用内嵌 IP:PORT）。</li>
 *   <li>{@code ip}：执行器 IP（默认为空自动获取）。</li>
 *   <li>{@code port}：执行器端口（默认 9999，&lt;=0 自动获取）。</li>
 *   <li>{@code logpath}：执行器日志路径（默认 ./logs/xxl-job/jobhandler）。</li>
 *   <li>{@code logretentiondays}：日志保存天数（默认 30，&gt;=3 生效，-1 关闭自动清理）。</li>
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
 *     <td>使用默认值</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>xxl-job 执行器参数来源。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>未做校验，依赖 xxl-job 框架行为。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>默认值</b></p>
 * <ul>
 *   <li>port 默认 9999，logpath 默认 {@code ./logs/xxl-job/jobhandler}，logretentiondays 默认 30。</li>
 * </ul>
 *
 * @since 2025/11/29
 * @version 1.0
 */
@Data
@ConfigurationProperties("xxl.job.executor")
public class CXxlJobExecutorConfig {

    /**
     * 执行器AppName [选填]：执行器心跳注册分组依据；为空则关闭自动注册
     */
    String appname;

    /**
     * 执行器注册 [选填]：优先使用该配置作为注册地址，
     * 为空时使用内嵌服务 ”IP:PORT“ 作为注册地址。
     * 从而更灵活的支持容器类型执行器动态IP和动态映射端口问题。
     */
    String address;

    /**
     * 执行器IP [选填]：默认为空表示自动获取IP，
     * 多网卡时可手动设置指定IP，
     * 该IP不会绑定Host仅作为通讯使用；
     * 地址信息用于 "执行器注册" 和 "调度中心请求并触发任务"；
     */
    String ip;

    /**
     * 执行器端口号 [选填]：小于等于0则自动获取；
     * 默认端口为9999，单机部署多个执行器时，注意要配置不同执行器端口；
     */
    Integer port = 9999;

    /**
     * 执行器运行日志文件存储磁盘路径 [选填] ：
     * 需要对该路径拥有读写权限；为空则使用默认路径；
     * /data/applogs/xxl-job/jobhandler
     */
    String logpath = "./logs/xxl-job/jobhandler";

    /**
     * 执行器日志文件保存天数 [选填] ：
     * 过期日志自动清理, 限制值大于等于3时生效;
     * 否则, 如-1, 关闭自动清理功能；
     */
    Integer logretentiondays = 30;

}
