package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CHttpClientUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CHttpClientUtilsTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「超时常量 / 请求配置 / 连接池 / 客户端与工厂」多个维度组织。</li>
 *   <li>超时常量逐一断言数值；请求配置断言其内超时与常量一致；连接池断言最大连接数；</li>
 *   <li>客户端/工厂/策略断言非空。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对各常量的约定。</li>
 *   <li>依据测试方法（黑盒等价类）：配置常量值与组成断言。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：五个超时常量值；REQUEST_CONFIG 三超时；CONNECTION_MANAGER 最大连接数；KEEP_ALIVE_STRATEGY/</li>
 *   <li>HTTP_CLIENT/REQUEST_FACTORY 非空。</li>
 *   <li>未覆盖：真实 HTTP 请求往返、连接池并发行为（属集成行为，单测不构造网络请求）。</li>
 * </ul>
 * <h2>超时常量</h2>
 * <ul>
 *   <li>1.1 五个超时常量值正确（timeoutConstants）</li>
 * </ul>
 * <h2>请求配置</h2>
 * <ul>
 *   <li>2.1 REQUEST_CONFIG 非空且三超时与常量一致（requestConfig）</li>
 * </ul>
 * <h2>连接池</h2>
 * <ul>
 *   <li>3.1 CONNECTION_MANAGER 非空且最大连接数正确（connectionManager）</li>
 * </ul>
 * <h2>客户端与工厂</h2>
 * <ul>
 *   <li>4.1 KEEP_ALIVE_STRATEGY / HTTP_CLIENT / REQUEST_FACTORY 均非空（httpClientAndFactory）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CHttpClientUtilsTests {

    /**
     * 对应测试用例 1.1：五个超时常量值正确
     */
    @Test
    public void timeoutConstants() {

        Assertions.assertEquals(3000, CHttpClientUtils.CONNECTION_REQUEST_TIMEOUT);
        Assertions.assertEquals(3000, CHttpClientUtils.CONNECT_TIMEOUT);
        Assertions.assertEquals(30000, CHttpClientUtils.SOCKET_TIMEOUT);
        Assertions.assertEquals(10000, CHttpClientUtils.KEEP_ALIVE_TIMEOUT);
        Assertions.assertEquals(1000, CHttpClientUtils.MAX_TOTAL_CONNECTIONS);

    }

    /**
     * 对应测试用例 2.1：REQUEST_CONFIG 非空且三超时与常量一致
     */
    @Test
    public void requestConfig() {

        Assertions.assertNotNull(CHttpClientUtils.REQUEST_CONFIG);
        Assertions.assertEquals(CHttpClientUtils.CONNECTION_REQUEST_TIMEOUT,
                CHttpClientUtils.REQUEST_CONFIG.getConnectionRequestTimeout());
        Assertions.assertEquals(CHttpClientUtils.CONNECT_TIMEOUT,
                CHttpClientUtils.REQUEST_CONFIG.getConnectTimeout());
        Assertions.assertEquals(CHttpClientUtils.SOCKET_TIMEOUT,
                CHttpClientUtils.REQUEST_CONFIG.getSocketTimeout());

    }

    /**
     * 对应测试用例 3.1：CONNECTION_MANAGER 非空且最大连接数正确
     */
    @Test
    public void connectionManager() {

        Assertions.assertNotNull(CHttpClientUtils.CONNECTION_MANAGER);
        Assertions.assertEquals(CHttpClientUtils.MAX_TOTAL_CONNECTIONS,
                CHttpClientUtils.CONNECTION_MANAGER.getMaxTotal());

    }

    /**
     * 对应测试用例 4.1：KEEP_ALIVE_STRATEGY / HTTP_CLIENT / REQUEST_FACTORY 均非空
     */
    @Test
    public void httpClientAndFactory() {

        Assertions.assertNotNull(CHttpClientUtils.KEEP_ALIVE_STRATEGY);
        Assertions.assertNotNull(CHttpClientUtils.HTTP_CLIENT);
        Assertions.assertNotNull(CHttpClientUtils.REQUEST_FACTORY);

    }

}
