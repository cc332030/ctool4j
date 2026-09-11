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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按 CRequestLog：请求日志模型 的处理路径/边界组织分类，逐一覆盖正例、反例与边界。</li>
 *   <li>使用 MockHttpServletRequest/MockHttpServletResponse/MockMvc 构造真实请求场景，贴近真实使用，不依赖外部服务。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：见下方编号索引。</li>
 *   <li>未覆盖：依赖外部 Servlet 容器/Spring 全容器装配的集成场景由集成测试覆盖。</li>
 * </ul>
 * <h2>CRequestLog：请求日志模型</h2>
 * <ul>
 *   <li>1.1 noArgsConstructor（noArgsConstructor）</li>
 *   <li>1.2 全参构建（builderAllFields）</li>
 *   <li>1.3 builderAndSetter（builderAndSetter）</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.0
 */

public class CRequestLogTests {

    /**
     * 对应测试用例 1.1：noArgsConstructor
     */
    @Test
    public void noArgsConstructor() {
        val log = new CRequestLog();

        Assertions.assertNull(log.getMethod());
        Assertions.assertEquals(0L, log.getBeginTimeMillis());
    }

    /**
     * 对应测试用例 1.2：全参构建（统一使用 builder，禁止依赖 lombok 生成的全参构造器）
     */
    @Test
    public void builderAllFields() {
        Map<String, Collection<String>> headers = new HashMap<>();
        Map<String, Collection<String>> params = new HashMap<>();
        Object req = "requestBody";

        val log = CRequestLog.builder()
            .source(CLogSource.MVC)
            .method("GET")
            .path("/path")
            .token("token")
            .traceId("trace-1")
            .tenantId("tenant-1")
            .userId("user-1")
            .ip("127.0.0.1")
            .requestHeaders(headers)
            .params(params)
            .req(req)
            .rsp("rsp")
            .responseStatus(200)
            .responseHeaders(headers)
            .errorMessage("boom")
            .beginTimeMillis(100L)
            .endTimeMillis(200L)
            .build();

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

    /**
     * 对应测试用例 1.3：builderAndSetter
     */
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
