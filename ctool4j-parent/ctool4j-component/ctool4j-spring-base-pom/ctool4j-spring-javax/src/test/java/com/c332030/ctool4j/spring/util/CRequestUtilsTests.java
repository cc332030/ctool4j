package com.c332030.ctool4j.spring.util;

import com.c332030.ctool4j.interfaces.CHttpRequest;
import com.c332030.ctool4j.model.CHttpServletRequest;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * <p>
 * Description: CRequestUtilsTests
 * </p>
 * <p>
 * 使用 Mockito 模拟抽象层请求 {@link CHttpRequest}，验证本类（javax 侧）保留的容器相关方法。
 * 容器无关的取用逻辑已下沉公共类 {@link CHttpRequestUtils}，用例见 {@code CHttpRequestUtilsTests}。
 * </p>
 * <p>
 * 属性名取自两侧同名常量 {@link CHttpServletRequest#ERROR_STATUS_CODE}——两侧取值不同（javax / jakarta），
 * 业务模块（含测试）据此引用即不绑定 Servlet 包，切模块即切值。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证两侧取值不同的常量方法（错误状态码）的各条路径与边界。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对错误状态码的约定（取自 {@link CHttpServletRequest#ERROR_STATUS_CODE} 属性）。</li>
 *   <li>依据测试方法（等价类 / 边界 / 分支覆盖）。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：错误状态码的正常与缺失路径。</li>
 *   <li>未覆盖：取当前请求上下文（依赖 Spring 请求上下文）与真实容器/框架集成场景。</li>
 * </ul>
 * <h2>请求工具（javax 侧）</h2>
 * <ul>
 *   <li>1.1 验证错误状态码（对应测试方法 1.1-1.4）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.2
 */
class CRequestUtilsTests {

    // ---------- getErrorStatusCode ----------

    /**
     * 对应测试用例 1.1：属性存在时取该属性
     */
    @Test
    void testGetErrorStatusCode_present() {
        CHttpRequest request = Mockito.mock(CHttpRequest.class);
        Mockito.when(request.getAttribute(CHttpServletRequest.ERROR_STATUS_CODE)).thenReturn(500);
        Assertions.assertEquals("500", CRequestUtils.getErrorStatusCode(request));
    }

    /**
     * 对应测试用例 1.2：属性不存在时返回 null
     */
    @Test
    void testGetErrorStatusCode_absent() {
        CHttpRequest request = Mockito.mock(CHttpRequest.class);
        Mockito.when(request.getAttribute(CHttpServletRequest.ERROR_STATUS_CODE)).thenReturn(null);
        Assertions.assertNull(CRequestUtils.getErrorStatusCode(request));
    }

    // ---------- 常量与主代码取值同源 ----------

    /**
     * 对应测试用例 1.3：常量取自容器，不是测试里另抄的字面量
     *
     * <p>主代码 {@code CRequestUtils#getErrorStatusCode} 以 {@link CHttpServletRequest#ERROR_STATUS_CODE} 为键取属性；
     * 用桩请求记录主代码实际取的键，断言它与常量相等——两侧常量的取值由容器定义，此处不重抄。</p>
     */
    @Test
    void testGetErrorStatusCode_usesContainerConstant() {
        val request = Mockito.mock(CHttpRequest.class);

        CRequestUtils.getErrorStatusCode(request);

        Mockito.verify(request).getAttribute(CHttpServletRequest.ERROR_STATUS_CODE);
    }

    /**
     * 对应测试用例 1.4：常量确实来自容器（非空且为 Servlet 规范定义的状态码属性名）
     */
    @Test
    void testErrorStatusCodeConstant_isContainerDefined() {
        Assertions.assertNotNull(CHttpServletRequest.ERROR_STATUS_CODE);
        Assertions.assertTrue(CHttpServletRequest.ERROR_STATUS_CODE.endsWith("error.status_code"));
    }

}
