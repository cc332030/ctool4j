package com.c332030.ctool4j.web.test.controller;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.web.controller.CErrorController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * Description: CErrorControllerTests
 * </p>
 * <p>
 * 使用 Mockito 模拟 HttpServletRequest，覆盖直接访问 /error（无错误状态码）的兜底场景
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按 CErrorController.error：统一错误处理入口 的处理路径/边界组织分类，逐一覆盖正例、反例与边界。</li>
 *   <li>使用 MockHttpServletRequest/MockHttpServletResponse/Mockito 构造真实请求场景，贴近真实使用，不依赖外部服务。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：见下方编号索引。</li>
 *   <li>未覆盖：依赖外部 Servlet 容器/Spring 全容器装配的集成场景由集成测试覆盖。</li>
 * </ul>
 * <h2>CErrorController.error：统一错误处理入口</h2>
 * <ul>
 *   <li>1.1 testError_withStatusCode（testError_withStatusCode）</li>
 *   <li>1.2 testError_withoutStatusCode_default500（testError_withoutStatusCode_default500）</li>
 *   <li>1.3 testError_invalidStatusCode_default500（testError_invalidStatusCode_default500）</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.0
 */
class CErrorControllerTests {

    private final CErrorController controller = new CErrorController();

    // ---------- error ----------

    /**
     * 对应测试用例 1.1：testError_withStatusCode
     */
    @Test
    void testError_withStatusCode() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(500);

        CStrResult<Void> result = controller.error(request);

        Assertions.assertEquals("500", result.getCode());
    }

    /**
     * 对应测试用例 1.2：testError_withoutStatusCode_default500
     */
    @Test
    void testError_withoutStatusCode_default500() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(null);

        CStrResult<Void> result = Assertions.assertDoesNotThrow(() -> controller.error(request));

        Assertions.assertEquals(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), result.getCode());
    }

    /**
     * 对应测试用例 1.3：testError_invalidStatusCode_default500
     */
    @Test
    void testError_invalidStatusCode_default500() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn("abc");

        CStrResult<Void> result = Assertions.assertDoesNotThrow(() -> controller.error(request));

        Assertions.assertEquals(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), result.getCode());
    }

}
