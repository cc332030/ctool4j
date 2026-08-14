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
 * @since 2025/12/1
 */
@UtilityClass
public class CHttpClientUtils {

    /**
     * 请求超时
     */
    public final int CONNECTION_REQUEST_TIMEOUT = 3 * 1000;

    /**
     * 连接超时
     */
    public final int CONNECT_TIMEOUT = 3 * 1000;

    /**
     * 请求超时
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
