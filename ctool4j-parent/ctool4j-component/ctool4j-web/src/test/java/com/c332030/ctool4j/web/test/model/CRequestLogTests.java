package com.c332030.ctool4j.web.test.model;

import com.c332030.ctool4j.web.model.CRequestLog;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Description: CRequestLogTests
 * </p>
 *
 * <p>覆盖 CRequestLog 数据类：各构造方式与读写</p>
 *
 * @since 2026/8/16
 */
public class CRequestLogTests {

    @Test
    public void noArgsConstructor() {
        val log = new CRequestLog();

        Assertions.assertNull(log.getMethod());
        Assertions.assertEquals(0L, log.getBeginTimeMillis());
    }

    @Test
    public void allArgsConstructor() {
        Map<String, Collection<String>> headers = new HashMap<>();
        Map<String, Collection<String>> params = new HashMap<>();
        Map<String, Object> reqs = new HashMap<>();

        val log = new CRequestLog(
            "GET", "/path", "token", "trace-1", "tenant-1", "user-1", "127.0.0.1",
            headers, params, reqs, "rsp", "boom", 100L, 200L
        );

        Assertions.assertEquals("GET", log.getMethod());
        Assertions.assertEquals("/path", log.getPath());
        Assertions.assertEquals("token", log.getToken());
        Assertions.assertEquals("trace-1", log.getTraceId());
        Assertions.assertEquals("tenant-1", log.getTenantId());
        Assertions.assertEquals("user-1", log.getUserId());
        Assertions.assertEquals("127.0.0.1", log.getIp());
        Assertions.assertSame(headers, log.getHeaders());
        Assertions.assertSame(params, log.getParams());
        Assertions.assertSame(reqs, log.getReqs());
        Assertions.assertEquals("rsp", log.getRsp());
        Assertions.assertEquals("boom", log.getErrorMessage());
        Assertions.assertEquals(100L, log.getBeginTimeMillis());
        Assertions.assertEquals(200L, log.getEndTimeMillis());
    }

    @Test
    public void builderAndSetter() {
        val log = CRequestLog.builder()
            .method("POST")
            .build();
        log.setPath("/post");

        Assertions.assertEquals("POST", log.getMethod());
        Assertions.assertEquals("/post", log.getPath());
    }

}
