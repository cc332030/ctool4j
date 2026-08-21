package com.c332030.ctool4j.spring.security.core;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.web.session.SessionInformationExpiredEvent;

/**
 * <p>
 * Description: CSessionInformationExpiredStrategyTests
 * </p>
 * <p>
 * 覆盖会话过期处理：输出 401 与 "Expired" 提示。
 * 通过 Spring 的 Mock 请求/响应运行完整 writeJsonError 链路，不依赖 Spring 容器。
 * </p>
 *
 * @since 2026/8/17
 */
class CSessionInformationExpiredStrategyTests {

    private final CSessionInformationExpiredStrategy strategy = new CSessionInformationExpiredStrategy();

        /**
     * 对应测试用例 1.1
     */
    @Test
    void testOnExpiredSessionDetected_write401() throws Exception {
        // 正例：会话过期输出 401，文案为 Expired + 请求路径
        val request = new MockHttpServletRequest("GET", "/api/user");
        val response = new MockHttpServletResponse();

        val sessionInfo = new SessionInformation("admin", "sid", new java.util.Date());
        val event = new SessionInformationExpiredEvent(sessionInfo, request, response);

        strategy.onExpiredSessionDetected(event);

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        Assertions.assertTrue(response.getContentType().contains("application/json"));
        val content = response.getContentAsString();
        Assertions.assertTrue(content.contains("Expired"));
        Assertions.assertTrue(content.contains("/api/user"));
    }
}
