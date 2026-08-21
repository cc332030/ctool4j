package com.c332030.ctool4j.web.test.model;

import com.c332030.ctool4j.core.enums.CLogSource;
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
 * <p>`com.c332030.ctool4j.web.model.CRequestLog`（请求日志数据类）的测试用例，覆盖各构造方式与读写</p>
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
        Object req = "requestBody";

        val log = new CRequestLog(
            CLogSource.MVC, "GET", "/path", "token", "trace-1", "tenant-1", "user-1", "127.0.0.1",
            headers, params, req, "rsp", 200, headers, "boom", 100L, 200L
        );

        Assertions.assertEquals(CLogSource.MVC, log.getSource());
        Assertions.assertEquals("GET", log.getMethod());
        Assertions.assertEquals("/path", log.getPath());
        Assertions.assertEquals("token", log.getToken());
        Assertions.assertEquals("trace-1", log.getTraceId());
        Assertions.assertEquals("tenant-1", log.getTenantId());
        Assertions.assertEquals("user-1", log.getUserId());
        Assertions.assertEquals("127.0.0.1", log.getIp());
        Assertions.assertSame(headers, log.getRequestHeaders());
        Assertions.assertSame(params, log.getParams());
        Assertions.assertSame(req, log.getReq());
        Assertions.assertEquals("rsp", log.getRsp());
        Assertions.assertEquals(200, log.getResponseStatus());
        Assertions.assertSame(headers, log.getResponseHeaders());
        Assertions.assertEquals("boom", log.getErrorMessage());
        Assertions.assertEquals(100L, log.getBeginTimeMillis());
        Assertions.assertEquals(200L, log.getEndTimeMillis());
    }

    @Test
    public void builderAndSetter() {
        val log = CRequestLog.builder()
            .method("POST")
            .source(CLogSource.FEIGN)
            .build();
        log.setPath("/post");
        log.setSource(CLogSource.MVC);

        Assertions.assertEquals("POST", log.getMethod());
        Assertions.assertEquals("/post", log.getPath());
        Assertions.assertEquals(CLogSource.MVC, log.getSource());
    }

}
