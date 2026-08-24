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
 * @since 2026/8/24
 * @see "doc/design/web/CRequestLogBaseConfig.adoc"
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
