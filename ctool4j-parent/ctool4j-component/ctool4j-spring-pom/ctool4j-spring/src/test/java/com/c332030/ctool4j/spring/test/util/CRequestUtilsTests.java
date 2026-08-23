package com.c332030.ctool4j.spring.test.util;

import com.c332030.ctool4j.definition.function.CBiConsumer;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Description: CRequestUtilsTests
 * </p>
 * <p>
 * 使用 Mockito 模拟 HttpServletRequest，仅测试不依赖 Spring 容器/Web 上下文的纯逻辑方法。
 * </p>
 *
 * @since 2026/8/14
 */
class CRequestUtilsTests {

    // ---------- getIp ----------

        /**
     * 对应测试用例 1.1
     */
    @Test
    void testGetIp_forwardedFor_single() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4");
        Assertions.assertEquals("1.2.3.4", CRequestUtils.getIp(request));
    }

        /**
     * 对应测试用例 1.2
     */
    @Test
    void testGetIp_forwardedFor_multiple_takesFirst() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4, 5.6.7.8, 9.10.11.12");
        Assertions.assertEquals("1.2.3.4", CRequestUtils.getIp(request));
    }

        /**
     * 对应测试用例 1.3
     */
    @Test
    void testGetIp_noForwardedFor_remoteAddr() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        Mockito.when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        Assertions.assertEquals("10.0.0.1", CRequestUtils.getIp(request));
    }

        /**
     * 对应测试用例 1.4
     */
    @Test
    void testGetIp_forwardedForWhitespace_kept() {
        // 易错：StrUtil.isNotEmpty 对纯空格返回 true，直接返回原头，不回退 remoteAddr
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("X-Forwarded-For")).thenReturn("  ");
        Mockito.when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        Assertions.assertEquals("  ", CRequestUtils.getIp(request));
    }

    // ---------- getHeader / getHeaders ----------

        /**
     * 对应测试用例 1.5
     */
    @Test
    void testGetHeader() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("token")).thenReturn("abc");
        Assertions.assertEquals("abc", CRequestUtils.getHeader(request, "token"));
    }

        /**
     * 对应测试用例 1.6
     */
    @Test
    void testGetHeader_null() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("token")).thenReturn(null);
        Assertions.assertNull(CRequestUtils.getHeader(request, "token"));
    }

        /**
     * 对应测试用例 1.7
     */
    @Test
    void testGetHeaders() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeaders("h")).thenReturn(new Vector<>(Arrays.asList("a", "b")).elements());
        List<String> result = CRequestUtils.getHeaders(request, "h");
        Assertions.assertEquals(Arrays.asList("a", "b"), result);
    }

        /**
     * 对应测试用例 1.8
     */
    @Test
    void testGetHeaders_null() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeaders("h")).thenReturn(null);
        Assertions.assertEquals(Collections.emptyList(), CRequestUtils.getHeaders(request, "h"));
    }

    // ---------- getAttrStr ----------

        /**
     * 对应测试用例 1.9
     */
    @Test
    void testGetAttrStr_present() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getAttribute("k")).thenReturn("v");
        Assertions.assertEquals("v", CRequestUtils.getAttrStr(request, "k"));
    }

        /**
     * 对应测试用例 1.10
     */
    @Test
    void testGetAttrStr_nullAttribute_returnsNull() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getAttribute("k")).thenReturn(null);
        Assertions.assertNull(CRequestUtils.getAttrStr(request, "k"));
    }

        /**
     * 对应测试用例 1.11
     */
    @Test
    void testGetAttrStr_nonStringAttribute() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getAttribute("k")).thenReturn(123);
        Assertions.assertEquals("123", CRequestUtils.getAttrStr(request, "k"));
    }

    // ---------- getErrorStatusCode ----------

        /**
     * 对应测试用例 1.12
     */
    @Test
    void testGetErrorStatusCode_present() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(500);
        Assertions.assertEquals("500", CRequestUtils.getErrorStatusCode(request));
    }

        /**
     * 对应测试用例 1.13
     */
    @Test
    void testGetErrorStatusCode_absent() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(null);
        Assertions.assertNull(CRequestUtils.getErrorStatusCode(request));
    }

    // ---------- getHeaderThenDo ----------

        /**
     * 对应测试用例 1.14
     */
    @Test
    void testGetHeaderThenDo_emptyCollection_noAction() {
        AtomicInteger count = new AtomicInteger();
        CRequestUtils.getHeaderThenDo(Collections.emptyList(), (k, v) -> count.incrementAndGet());
        Assertions.assertEquals(0, count.get());
    }

        /**
     * 对应测试用例 1.15
     */
    @Test
    void testGetHeaderThenDo_nullCollection_noAction() {
        AtomicInteger count = new AtomicInteger();
        CRequestUtils.getHeaderThenDo(null, (k, v) -> count.incrementAndGet());
        Assertions.assertEquals(0, count.get());
    }

    // ---------- getHeadersThenDo ----------

        /**
     * 对应测试用例 1.16
     */
    @Test
    void testGetHeadersThenDo_emptyCollection_noAction() {
        AtomicInteger count = new AtomicInteger();
        CRequestUtils.getHeadersThenDo(Collections.emptyList(), (k, v) -> count.incrementAndGet());
        Assertions.assertEquals(0, count.get());
    }

        /**
     * 对应测试用例 1.17
     */
    @Test
    void testGetHeadersThenDo_nullCollection_noAction() {
        AtomicInteger count = new AtomicInteger();
        CRequestUtils.getHeadersThenDo(null, (k, v) -> count.incrementAndGet());
        Assertions.assertEquals(0, count.get());
    }

    // ---------- addPrepare / prepare ----------

        /**
     * 对应测试用例 1.18
     */
    @Test
    void testAddPrepareAndPrepare() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        AtomicInteger count = new AtomicInteger();
        CBiConsumer<HttpServletRequest, HttpServletResponse> consumer = (req, res) -> count.incrementAndGet();
        CRequestUtils.addPrepare(consumer);
        CRequestUtils.prepare(request, response);
        CRequestUtils.prepare(request, response);
        Assertions.assertEquals(2, count.get());
    }

        /**
     * 对应测试用例 1.19
     */
    @Test
    void testPrepare_exceptionSwallowed() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        CBiConsumer<HttpServletRequest, HttpServletResponse> consumer = (req, res) -> {
            throw new RuntimeException("boom");
        };
        CRequestUtils.addPrepare(consumer);
        Assertions.assertDoesNotThrow(() -> CRequestUtils.prepare(request, response));
    }

        /**
     * 对应测试用例 1.20
     */
    @Test
    void testAddPrepare_nullConsumer_throwsNPE() {
        Assertions.assertThrowsExactly(NullPointerException.class, () -> CRequestUtils.addPrepare(null));
    }

    // ---------- addClear / clear ----------

        /**
     * 对应测试用例 1.21
     */
    @Test
    void testAddClearAndClear() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        AtomicInteger count = new AtomicInteger();
        CBiConsumer<HttpServletRequest, HttpServletResponse> consumer = (req, res) -> count.incrementAndGet();
        CRequestUtils.addClear(consumer);
        CRequestUtils.clear(request, response);
        CRequestUtils.clear(request, response);
        Assertions.assertEquals(2, count.get());
    }

        /**
     * 对应测试用例 1.22
     */
    @Test
    void testClear_exceptionSwallowed() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        CBiConsumer<HttpServletRequest, HttpServletResponse> consumer = (req, res) -> {
            throw new RuntimeException("boom");
        };
        CRequestUtils.addClear(consumer);
        Assertions.assertDoesNotThrow(() -> CRequestUtils.clear(request, response));
    }

        /**
     * 对应测试用例 1.23
     */
    @Test
    void testAddClear_nullConsumer_throwsNPE() {
        Assertions.assertThrowsExactly(NullPointerException.class, () -> CRequestUtils.addClear(null));
    }
}
