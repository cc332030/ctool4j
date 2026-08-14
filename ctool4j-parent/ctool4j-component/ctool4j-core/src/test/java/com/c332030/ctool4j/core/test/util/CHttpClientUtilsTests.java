package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CHttpClientUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CHttpClientUtilsTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CHttpClientUtilsTests {

    @Test
    public void timeoutConstants() {

        Assertions.assertEquals(3000, CHttpClientUtils.CONNECTION_REQUEST_TIMEOUT);
        Assertions.assertEquals(3000, CHttpClientUtils.CONNECT_TIMEOUT);
        Assertions.assertEquals(30000, CHttpClientUtils.SOCKET_TIMEOUT);
        Assertions.assertEquals(10000, CHttpClientUtils.KEEP_ALIVE_TIMEOUT);
        Assertions.assertEquals(1000, CHttpClientUtils.MAX_TOTAL_CONNECTIONS);

    }

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

    @Test
    public void connectionManager() {

        Assertions.assertNotNull(CHttpClientUtils.CONNECTION_MANAGER);
        Assertions.assertEquals(CHttpClientUtils.MAX_TOTAL_CONNECTIONS,
                CHttpClientUtils.CONNECTION_MANAGER.getMaxTotal());

    }

    @Test
    public void httpClientAndFactory() {

        Assertions.assertNotNull(CHttpClientUtils.KEEP_ALIVE_STRATEGY);
        Assertions.assertNotNull(CHttpClientUtils.HTTP_CLIENT);
        Assertions.assertNotNull(CHttpClientUtils.REQUEST_FACTORY);

    }

}
