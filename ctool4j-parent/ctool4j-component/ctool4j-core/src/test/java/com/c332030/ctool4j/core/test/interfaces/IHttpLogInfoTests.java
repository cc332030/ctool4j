package com.c332030.ctool4j.core.test.interfaces;

import com.c332030.ctool4j.core.interfaces.IHttpLogInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: IHttpLogInfoTests
 * </p>
 *
 * @since 2025/12/12
 */
public class IHttpLogInfoTests {

    @Test
    public void requiredMethods() {

        IHttpLogInfo info = new IHttpLogInfo() {
            @Override
            public String getMethod() {
                return "POST";
            }

            @Override
            public String getPath() {
                return "/api/test";
            }
        };

        Assertions.assertEquals("POST", info.getMethod());
        Assertions.assertEquals("/api/test", info.getPath());

    }

    @Test
    public void defaults() {

        IHttpLogInfo info = new IHttpLogInfo() {
            @Override
            public String getMethod() {
                return "GET";
            }

            @Override
            public String getPath() {
                return "/api/test";
            }
        };

        Assertions.assertNull(info.getToken());
        Assertions.assertNull(info.getTraceId());
        Assertions.assertNull(info.getTenantId());
        Assertions.assertNull(info.getUserId());
        Assertions.assertNull(info.getIp());
        Assertions.assertNull(info.getRsp());
        Assertions.assertNull(info.getErrorMessage());

        Assertions.assertTrue(info.getHeaders().isEmpty());
        Assertions.assertTrue(info.getParams().isEmpty());
        Assertions.assertTrue(info.getReqs().isEmpty());

        Assertions.assertEquals(0L, info.getBeginTimeMillis());
        Assertions.assertEquals(0L, info.getEndTimeMillis());

    }

    @Test
    public void overridden() {

        IHttpLogInfo info = new IHttpLogInfo() {
            @Override
            public String getMethod() {
                return "PUT";
            }

            @Override
            public String getPath() {
                return "/api/update";
            }

            @Override
            public String getToken() {
                return "token-abc";
            }

            @Override
            public long getBeginTimeMillis() {
                return 100L;
            }
        };

        Assertions.assertEquals("token-abc", info.getToken());
        Assertions.assertEquals(100L, info.getBeginTimeMillis());

    }

}
