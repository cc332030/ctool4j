package com.c332030.ctool4j.web.model;

import com.c332030.ctool4j.core.interfaces.ICSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Collection;
import java.util.Map;

/**
 * <p>
 * Description: CRequestLog
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CRequestLog} 为请求日志模型，{@code @Data} + {@code @SuperBuilder} + {@code @NoArgsConstructor} + {@code @AllArgsConstructor}， 承载一次 HTTP 请求/响应的完整日志信息。</p>
 * <p>字段：</p>
 * <ul>
 *   <li>{@code source}：日志来源（{@code ICSource}，如 feign、mvc），用于日志最前面标识请求来源</li>
 *   <li>{@code method}：HTTP 方法（GET/POST/PUT/DELETE...）</li>
 *   <li>{@code path}：请求路径（不含 query string）</li>
 *   <li>{@code token}：认证令牌（Authorization 请求头的值），仅用于日志末尾业务数据区展示</li>
 *   <li>{@code traceId}/{@code tenantId}/{@code userId}：链路追踪 ID、租户 ID、用户 ID</li>
 *   <li>{@code ip}：请求来源 IP（仅用于日志展示的元信息，非 HTTP 请求头）</li>
 *   <li>{@code requestHeaders}：完整请求头（headerName → 一个或多个 headerValue），供 feign 等客户端请求日志使用，</li>
 *   <li>仅包含真实请求头；token/traceId 等业务数据见 {@code CCommUtils.appendHttpLog}</li>
 *   <li>{@code params}：query 参数（仅 GET 时拼接到 URL）</li>
 *   <li>{@code req}：请求体（统一为 Object），服务端 MVC 经 Advice 记录请求体对象，feign 记录请求体文本字符串</li>
 *   <li>{@code rsp}：响应体，拼接时经 {@code getPrintAble} 可打印处理后输出</li>
 *   <li>{@code responseStatus}：响应状态码（如 200、404），未采集时为 null（不输出状态行）</li>
 *   <li>{@code responseHeaders}：响应头，为空时不输出</li>
 *   <li>{@code errorMessage}：异常信息</li>
 *   <li>{@code beginTimeMillis}/{@code endTimeMillis}：请求开始/结束时间（毫秒时间戳），{@code @Builder.Default} 默认 0</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>{@code responseStatus} 为 null</td>
 *     <td>不输出响应状态行</td>
 *   </tr>
 *   <tr>
 *     <td>{@code responseHeaders} 为空</td>
 *     <td>不输出响应头</td>
 *   </tr>
 *   <tr>
 *     <td>时间戳默认 0</td>
 *     <td>耗时计算为 0，由拼接层判断</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>服务端 MVC、feign、resttemplate、httpclient 等请求方式构造请求日志统一打印。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>{@code requestHeaders} 仅含真实请求头，业务数据（token/traceId）由 {@code CCommUtils.appendHttpLog} 业务数据区输出。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>数据模型</b></p>
 * <ul>
 *   <li>请求/响应体统一为 {@code Object}，兼容 MVC 记录对象与 feign 记录文本，拼接时统一处理。</li>
 * </ul>
 * <p><b>默认耗时</b></p>
 * <ul>
 *   <li>时间戳默认 0，未测量时 {@code end - begin} 为 0，由 {@code CCommUtils} 判断不输出耗时。</li>
 * </ul>
 *
 * @author c332030
 * @since 2024/5/6
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CRequestLog {

    /**
     * 日志来源（如 feign、mvc 等），用于日志最前面标识请求来源
     */
    ICSource source;

    /**
     * HTTP 方法：GET/POST/PUT/DELETE...
     */
    String method;

    /**
     * 请求路径（不含 query string）
     */
    String path;

    /**
     * 认证令牌（Authorization 请求头的值），仅用于日志末尾业务数据区展示
     */
    String token;

    /**
     * 链路追踪 ID
     */
    String traceId;

    /**
     * 租户 ID
     */
    String tenantId;

    /**
     * 用户 ID
     */
    String userId;

    /**
     * 请求来源 IP（仅用于日志展示的元信息，非 HTTP 请求头）
     */
    String ip;

    /**
     * 完整请求头（headerName → 一个或多个 headerValue），
     * 供 feign 等客户端请求日志使用，仅包含真实请求头；token/traceId 等应用业务数据见 CCommUtils.appendHttpLog
     */
    Map<String, Collection<String>> requestHeaders;

    /**
     * query 参数（仅 GET 时拼接到 URL）
     */
    Map<String, Collection<String>> params;

    /**
     * 请求体（统一为 Object）：服务端 MVC 经 CLogRequestBodyAdvice 记录请求体对象，
     * feign 客户端记录请求体文本字符串，拼接时统一从 req 取请求体
     */
    Object req;

    /**
     * 响应体：拼接时经 getPrintAble 可打印处理后输出（见 CCommUtils.appendHttpLog）
     */
    Object rsp;

    /**
     * 响应状态码（如 200、404），用于输出响应状态行；未采集时为 null（不输出状态行）
     */
    Integer responseStatus;

    /**
     * 响应头（headerName → 一个或多个 headerValue），用于输出响应报文头；为空时不输出
     */
    Map<String, Collection<String>> responseHeaders;

    /**
     * 异常信息
     */
    String errorMessage;

    /**
     * 请求开始时间（毫秒时间戳），用于计算耗时；未测量时默认 0
     */
    @Builder.Default
    Long beginTimeMillis = 0L;

    /**
     * 请求结束时间（毫秒时间戳），用于计算耗时；未测量时默认 0
     */
    @Builder.Default
    Long endTimeMillis = 0L;

}
