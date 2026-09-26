package com.c332030.ctool4j.core.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

/**
 * <p>
 * Description: CHttpClientUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CHttpClientUtils} 为 HTTP 客户端配置工具类，提供全局可复用的 HttpClient 组件常量：</p>
 * <ul>
 *   <li>超时常量：{@code CONNECTION_REQUEST_TIMEOUT=3000}、{@code CONNECT_TIMEOUT=3000}、{@code SOCKET_TIMEOUT=30000}、</li>
 *   <li>{@code KEEP_ALIVE_TIMEOUT=10000}、{@code MAX_TOTAL_CONNECTIONS=1000}</li>
 *   <li>{@code REQUEST_CONFIG}：默认请求配置（含上述超时）</li>
 *   <li>{@code CONNECTION_MANAGER}：连接池管理器（最大连接数/单路由并发）</li>
 *   <li>{@code KEEP_ALIVE_STRATEGY}：长连接策略</li>
 *   <li>{@code HTTP_CLIENT}：全局 HTTP 客户端（禁用自动重试）</li>
 *   <li>{@code REQUEST_FACTORY}：Spring {@code ClientHttpRequestFactory}</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>长连接服务端未给建议/建议不足</td>
 *     <td>使用 KEEP_ALIVE_TIMEOUT=10s</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>Spring RestTemplate/WebClient 等需要统一超时与连接池的 HTTP 客户端配置。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>常量配置全局共享，单业务特殊超时需另建客户端。</li>
 *   <li>禁用自动重试，需要重试时自行配置。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>全局单例 HttpClient，连接池共享，减少连接创建开销。</li>
 *   <li>禁用自动重试，避免重试导致的重复请求副作用，由调用方决策是否重试。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>连接池</b></p>
 * <ul>
 *   <li>{@code PoolingHttpClientConnectionManager} 设 {@code setMaxTotal(1000)} 与</li>
 *   <li>{@code setDefaultMaxPerRoute(1000)}（显式提升单路由并发，避免并发被路由级默认 2 限制）。</li>
 * </ul>
 * <p><b>长连接策略</b></p>
 * <ul>
 *   <li>基于 {@code DefaultConnectionKeepAliveStrategy.INSTANCE} 获取服务端建议时长，不足（&lt;=0）时</li>
 *   <li>使用 {@code KEEP_ALIVE_TIMEOUT=10s} 兜底。</li>
 * </ul>
 * <p><b>客户端与工厂</b></p>
 * <ul>
 *   <li>{@code HTTP_CLIENT} 禁用自动重试、设置默认请求配置、连接池与长连接策略。</li>
 *   <li>{@code REQUEST_FACTORY} 基于 HTTP_CLIENT 构建 Spring 请求工厂，供 Spring 模板使用。</li>
 * </ul>
 *
 * @since 2025/12/1
 * @version 1.0
 */
@UtilityClass
public class CHttpClientUtils {

    /**
     * 从连接池获取连接的超时时间（毫秒）
     */
    public final int CONNECTION_REQUEST_TIMEOUT = 3 * 1000;

    /**
     * 连接超时
     */
    public final int CONNECT_TIMEOUT = 3 * 1000;

    /**
     * socket 读取数据的超时时间（毫秒）
     */
    public final int SOCKET_TIMEOUT = 30 * 1000;

    /**
     * 长连接超时
     */
    public final int KEEP_ALIVE_TIMEOUT = 10 * 1000;

    /**
     * 最大连接数
     */
    public final int MAX_TOTAL_CONNECTIONS = 1000;

    /**
     * 默认请求配置
     */
    public final RequestConfig REQUEST_CONFIG = RequestConfig.custom()
            .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
            .setConnectTimeout(CONNECT_TIMEOUT)
            .setSocketTimeout(SOCKET_TIMEOUT)
            .build();

    /**
     * 连接池管理器
     */
    public final PoolingHttpClientConnectionManager CONNECTION_MANAGER;
    static {
        CONNECTION_MANAGER = new PoolingHttpClientConnectionManager();
        CONNECTION_MANAGER.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        // 单路由默认并发 2，显式提升至最大连接数，避免并发请求被路由级限制
        CONNECTION_MANAGER.setDefaultMaxPerRoute(MAX_TOTAL_CONNECTIONS);
    }

    /**
     * 长连接策略（保持时间不足时使用默认超时）
     */
    public final ConnectionKeepAliveStrategy KEEP_ALIVE_STRATEGY = (response, context) -> {
        val timeout = DefaultConnectionKeepAliveStrategy.INSTANCE.getKeepAliveDuration(response, context);
        return timeout > 0 ? timeout : KEEP_ALIVE_TIMEOUT;
    };

    /**
     * HTTP 客户端
     */
    public final HttpClient HTTP_CLIENT = HttpClients.custom()
            .disableAutomaticRetries()
            .setDefaultRequestConfig(REQUEST_CONFIG)
            .setConnectionManager(CONNECTION_MANAGER)
            .setKeepAliveStrategy(KEEP_ALIVE_STRATEGY)
            .build();

    /**
     * Spring 请求工厂
     */
    public final ClientHttpRequestFactory REQUEST_FACTORY = new HttpComponentsClientHttpRequestFactory(HTTP_CLIENT);

}
