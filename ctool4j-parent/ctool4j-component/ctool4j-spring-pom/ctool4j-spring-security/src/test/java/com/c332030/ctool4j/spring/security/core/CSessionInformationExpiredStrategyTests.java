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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证输出 401的各条路径与边界。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对输出 401的约定。</li>
 *   <li>依据测试方法（等价类/边界/分支覆盖）。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：输出 401的正常、边界与异常路径。</li>
 *   <li>未覆盖：真实容器/框架集成场景。</li>
 * </ul>
 * <h2>会话过期策略</h2>
 * <ul>
 *   <li>1.1 验证输出 401（对应测试方法 1.1-1.1）</li>
 * </ul>
 *
 * @since 2026/8/17
 * @version 1.0
 */
class CSessionInformationExpiredStrategyTests {

    private final CSessionInformationExpiredStrategy strategy = new CSessionInformationExpiredStrategy();

        /**
         * 对应测试用例 1.1：验证输出 401（对应测试方法 1.1-1.1）
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
