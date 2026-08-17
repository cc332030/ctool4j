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
 * @since 2026/8/16
 */
class CErrorControllerTests {

    private final CErrorController controller = new CErrorController();

    // ---------- error ----------

    @Test
    void testError_withStatusCode() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(500);

        CStrResult<Void> result = controller.error(request);

        Assertions.assertEquals("500", result.getCode());
    }

    @Test
    void testError_withoutStatusCode_default500() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(null);

        CStrResult<Void> result = Assertions.assertDoesNotThrow(() -> controller.error(request));

        Assertions.assertEquals(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), result.getCode());
    }

    @Test
    void testError_invalidStatusCode_default500() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn("abc");

        CStrResult<Void> result = Assertions.assertDoesNotThrow(() -> controller.error(request));

        Assertions.assertEquals(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), result.getCode());
    }

}
