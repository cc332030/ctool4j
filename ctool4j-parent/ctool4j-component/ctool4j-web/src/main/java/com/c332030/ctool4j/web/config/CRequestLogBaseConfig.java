package com.c332030.ctool4j.web.config;

import lombok.Data;

/**
 * <p>
 * Description: CRequestLogBaseConfig
 * </p>
 * 请求日志公共配置基类，抽取 web/feign 等请求日志共用的开关属性：
 * enable（请求日志总开关）、enableHeader（请求头日志开关）、slowLogEnable/slowLogMillis（慢请求日志）。
 * 各场景特有属性由子类各自维护，子类通过 {@code @ConfigurationProperties} 绑定各自配置前缀。
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code enable}：请求日志总开关，默认 {@code false}</li>
 *   <li>{@code enableHeader}：请求头日志开关，默认 {@code false}（请求头可能含 Authorization、Cookie 等敏感信息，需时显式开启）</li>
 *   <li>{@code slowLogEnable}：慢请求日志开关，默认 {@code true}（默认启用，不受 enable 总开关控制）</li>
 *   <li>{@code slowLogMillis}：慢请求日志毫秒数，默认 3000</li>
 * </ul>
 * <p>各场景特有属性由子类各自维护：</p>
 * <ul>
 *   <li>{@code CRequestLogConfig}（web）：{@code excludeUriPatterns}</li>
 *   <li>{@code CFeignClientLogConfig}（feign）：{@code logAll}、{@code apiWhiteList}/{@code apiBlackList}、{@code hostWhiteList}/{@code hostBlackList}、{@code pathWhiteList}/{@code pathBlackList}</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>未配置 {@code enable}</td>
 *     <td>默认 false，不记录请求日志</td>
 *   </tr>
 *   <tr>
 *     <td>未配置 {@code enableHeader}</td>
 *     <td>默认 false，不输出请求头</td>
 *   </tr>
 *   <tr>
 *     <td>未配置 {@code slowLogEnable}</td>
 *     <td>默认 true，慢日志启用</td>
 *   </tr>
 *   <tr>
 *     <td>未配置 {@code slowLogMillis}</td>
 *     <td>默认 3000</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>服务端 MVC、feign 等请求日志配置共用的开关属性统一维护。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>基类仅抽取真正公共的属性（enable/enableHeader/slowLogEnable/slowLogMillis）；{@code enableCost} 在 feign 中未被任何业务代码使用，已随重构删除（仅测试引用，见变更日志）。</li>
 *   <li>集合型特有属性（白/黑名单）仍留在 feign 子类，不因抽象基类而上移。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>公共属性上移</b></p>
 * <ul>
 *   <li>将两个具体配置类共有的 {@code enable}/{@code enableHeader}/{@code slowLogEnable}/{@code slowLogMillis} 上移到基类，子类通过 {@code extends} 继承，避免重复声明。</li>
 *   <li>子类各自保留 {@code @ConfigurationProperties} 绑定各自前缀（web：{@code logging.request-log}，feign：{@code feign.client.log}），基类不声明 {@code @ConfigurationProperties}。</li>
 * </ul>
 * <p><b>lombok 生成访问器与 toString</b></p>
 * <ul>
 *   <li>基类标注 {@code @Data} 生成公共字段的 getter/setter。</li>
 *   <li>项目 {@code lombok.config} 全局配置 {@code lombok.toString.callSuper=call}、{@code lombok.equalsAndHashCode.callSuper=call}：子类 {@code @Data} 自动在 toString/equals/hashCode 中包含父类公共字段（保证调试可见完整配置），子类<b>无需</b>再显式标注 {@code @ToString(callSuper=true)}/{@code @EqualsAndHashCode(callSuper=true)}。</li>
 * </ul>
 *
 * @since 2026/8/24
 * @version 1.0
 */
@Data
public class CRequestLogBaseConfig {

    /**
     * 请求日志开关
     */
    Boolean enable = false;

    /**
     * 请求头日志开关
     * <p>默认关闭：请求头可能含 Authorization、Cookie 等敏感信息，需要时显式开启；
     * traceId/tenantId/userId 等业务数据不受此开关影响，仍由业务数据区输出；
     * token/ip 与开关联动：开关开启时请求头已输出 Authorization/ip 不重复打印，开关关闭时业务数据区输出 token/ip 保证可见</p>
     */
    Boolean enableHeader = false;

    /**
     * 慢请求日志-开关
     * <p>默认启用，不受 enable 总开关控制：只要采集到请求开始时间，超时即输出慢日志</p>
     */
    Boolean slowLogEnable = true;

    /**
     * 慢请求日志-毫秒数
     */
    Integer slowLogMillis = 3000;

}
