package com.c332030.ctool4j.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

/**
 * <p>
 * Description: CRequestLogConfig
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CRequestLogConfig} 为服务端 MVC 请求日志配置属性类，{@code @ConfigurationProperties("logging.request-log")} + {@code @Data}， 由 Spring Boot 绑定 {@code logging.request-log.*} 前缀配置项。公共属性（{@code enable}/{@code enableHeader}/{@code slowLogEnable}/{@code slowLogMillis}）继承自 {@code CRequestLogBaseConfig}，本类维护服务端特有属性：</p>
 * <ul>
 *   <li>{@code excludeUriPatterns}：排除的 URI 列表（支持通配符 {@code *}）</li>
 * </ul>
 * <p>继承自基类的属性：</p>
 * <ul>
 *   <li>{@code enable}：请求日志开关，默认 {@code false}</li>
 *   <li>{@code enableHeader}：请求头日志开关，默认 {@code false}（请求头可能含 Authorization、Cookie 等敏感信息，</li>
 *   <li>需时显式开启；traceId/tenantId/userId 等业务数据不受此开关影响，仍由业务数据区输出；</li>
 *   <li>token/ip 与开关联动：开关开启时请求头已输出 Authorization/ip 不重复打印，开关关闭时业务数据区输出 token/ip 保证可见）</li>
 *   <li>{@code slowLogEnable}：慢请求日志开关，默认 {@code true}（默认启用，不受 enable 控制）</li>
 *   <li>{@code slowLogMillis}：慢请求日志毫秒数，默认 3000</li>
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
 *     <td>未配置 {@code excludeUriPatterns}</td>
 *     <td>为 null，{@code isExcludeUri} 判空后返回 false（不排除任何 URI）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>通过 {@code logging.request-log.*} 配置请求日志开关、敏感头、慢请求阈值与排除 URI。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>仅配置模型，实际生效依赖 {@code CRequestLogUtils}/拦截器等读取判断。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>{@code enableHeader} 与业务数据（token/ip）联动：开关开启时请求头已输出 Authorization/ip，业务数据区不重复打印；开关关闭时业务数据区输出 token/ip 保证可见（有意设计）。</li>
 *   <li>{@code excludeUriPatterns} 支持 {@code *} 通配，通过 {@code CPatternUtils.getUrlCache} 预编译匹配。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>配置绑定</b></p>
 * <ul>
 *   <li>使用 {@code logging.request-log} 前缀绑定，默认值即按安全默认（enable=false、enableHeader=false）。</li>
 * </ul>
 * <p><b>公共属性继承</b></p>
 * <ul>
 *   <li>{@code enable}/{@code enableHeader}/{@code slowLogEnable}/{@code slowLogMillis} 上移至 {@code CRequestLogBaseConfig}，本类仅保留服务端特有属性（排除 URI）。</li>
 * </ul>
 * <p><b>安全默认</b></p>
 * <ul>
 *   <li>{@code enableHeader} 默认关闭，避免敏感请求头（Authorization/Cookie）泄露；业务数据区（token/ip）</li>
 *   <li>与开关联动：开关关闭时仍输出 token/ip，保证鉴权与来源信息可见。</li>
 * </ul>
 *
 * @since 2025/9/29
 * @version 1.0
 */
@Data
@ConfigurationProperties("logging.request-log")
public class CRequestLogConfig extends CRequestLogBaseConfig {

    /**
     * 排除的URI列表（支持通配符 *）
     */
    Set<String> excludeUriPatterns;

}
