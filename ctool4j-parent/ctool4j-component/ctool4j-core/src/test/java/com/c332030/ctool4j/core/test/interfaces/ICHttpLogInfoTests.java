package com.c332030.ctool4j.core.test.interfaces;

import com.c332030.ctool4j.core.interfaces.ICHttpLogInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: ICHttpLogInfoTests
 * </p>
 *
 * @since 2025/12/12
 */
public class ICHttpLogInfoTests {

    @Test
    public void requiredMethods() {

        ICHttpLogInfo info = new ICHttpLogInfo() {
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

        ICHttpLogInfo info = new ICHttpLogInfo() {
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
        Assertions.assertNull(info.getSource());

        Assertions.assertTrue(info.getHeaders().isEmpty());
        Assertions.assertTrue(info.getParams().isEmpty());
        Assertions.assertNull(info.getReq());

        Assertions.assertEquals(0L, info.getBeginTimeMillis());
        Assertions.assertEquals(0L, info.getEndTimeMillis());

    }

    @Test
    public void overridden() {

        ICHttpLogInfo info = new ICHttpLogInfo() {
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
