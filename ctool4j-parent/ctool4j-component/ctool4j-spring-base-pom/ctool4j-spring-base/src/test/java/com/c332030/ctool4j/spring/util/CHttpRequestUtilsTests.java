package com.c332030.ctool4j.spring.util;

import com.c332030.ctool4j.definition.function.CBiConsumer;
import com.c332030.ctool4j.interfaces.CHttpRequest;
import com.c332030.ctool4j.interfaces.CHttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Description: CHttpRequestUtilsTests
 * </p>
 * <p>
 * 使用 Mockito 模拟抽象层请求 {@link CHttpRequest}（不引入任何 Servlet 包），验证容器无关的请求取用逻辑。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证 IP / 头 / 属性 / 取值后动作 / 请求前后回调的各条路径与边界。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 IP / 头 / 属性 / 回调的约定。</li>
 *   <li>依据测试方法（等价类 / 边界 / 分支覆盖）。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：IP / 头 / 属性 / 取值后动作 / prepare / clear 的正常、边界与异常路径。</li>
 *   <li>未覆盖：取当前请求上下文的路径（需容器包装，属两侧同名类 {@code CRequestUtils} 的职责）。</li>
 * </ul>
 * <h2>请求工具（容器无关部分）</h2>
 * <ul>
 *   <li>1.1 验证 IP / 头 / 属性（对应测试方法 1.1-1.13）</li>
 *   <li>1.2 验证取值后动作与请求前后回调（对应测试方法 1.14-1.21）</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.0
 */
class CHttpRequestUtilsTests {

    /**
     * 构造「客户端 IP 解析走真实默认实现」的请求 mock
     *
     * <p>{@code getClientIp} 是接口的默认方法，Mockito 的普通 mock 不会执行默认实现，故显式 {@code thenCallRealMethod}——
     * 默认实现内部仍走 mock 的 {@code getHeader}/{@code getRemoteAddr}，可正常打桩（无需引入 Servlet 包）。</p>
     *
     * @return 请求 mock
     */
    private CHttpRequest mockRequestWithClientIp() {
        CHttpRequest request = Mockito.mock(CHttpRequest.class);
        Mockito.when(request.getClientIp()).thenCallRealMethod();
        return request;
    }

    // ---------- getIp ----------

    /**
     * 对应测试用例 1.1：X-Forwarded-For 单段时取该段
     */
    @Test
    void testGetIp_forwardedFor_single() {
        CHttpRequest request = mockRequestWithClientIp();
        Mockito.when(request.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4");
        Assertions.assertEquals("1.2.3.4", CHttpRequestUtils.getIp(request));
    }

    /**
     * 对应测试用例 1.2：X-Forwarded-For 多段时取首段
     */
    @Test
    void testGetIp_forwardedFor_multiple_takesFirst() {
        CHttpRequest request = mockRequestWithClientIp();
        Mockito.when(request.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4, 5.6.7.8, 9.10.11.12");
        Assertions.assertEquals("1.2.3.4", CHttpRequestUtils.getIp(request));
    }

    /**
     * 对应测试用例 1.3：无 X-Forwarded-For 时回退 remoteAddr
     */
    @Test
    void testGetIp_noForwardedFor_remoteAddr() {
        CHttpRequest request = mockRequestWithClientIp();
        Mockito.when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        Mockito.when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        Assertions.assertEquals("10.0.0.1", CHttpRequestUtils.getIp(request));
    }

    /**
     * 对应测试用例 1.4
     */
    @Test
    void testGetIp_forwardedForWhitespace_kept() {
        // 易错：空串判断不 Trim，纯空格视为有值，直接返回原头、不回退 remoteAddr
        CHttpRequest request = mockRequestWithClientIp();
        Mockito.when(request.getHeader("X-Forwarded-For")).thenReturn("  ");
        Mockito.when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        Assertions.assertEquals("  ", CHttpRequestUtils.getIp(request));
    }

    // ---------- getHeader / getHeaders ----------

    /**
     * 对应测试用例 1.5
     */
    @Test
    void testGetHeader() {
        CHttpRequest request = Mockito.mock(CHttpRequest.class);
        Mockito.when(request.getHeader("token")).thenReturn("abc");
        Assertions.assertEquals("abc", CHttpRequestUtils.getHeader(request, "token"));
    }

    /**
     * 对应测试用例 1.6
     */
    @Test
    void testGetHeader_null() {
        CHttpRequest request = Mockito.mock(CHttpRequest.class);
        Mockito.when(request.getHeader("token")).thenReturn(null);
        Assertions.assertNull(CHttpRequestUtils.getHeader(request, "token"));
    }

    /**
     * 对应测试用例 1.7
     */
    @Test
    void testGetHeaders() {
        CHttpRequest request = Mockito.mock(CHttpRequest.class);
        Mockito.when(request.getHeaders("h")).thenReturn(new Vector<>(Arrays.asList("a", "b")).elements());
        List<String> result = CHttpRequestUtils.getHeaders(request, "h");
        Assertions.assertEquals(Arrays.asList("a", "b"), result);
    }

    /**
     * 对应测试用例 1.8
     */
    @Test
    void testGetHeaders_null() {
        CHttpRequest request = Mockito.mock(CHttpRequest.class);
        Mockito.when(request.getHeaders("h")).thenReturn(null);
        Assertions.assertEquals(Collections.emptyList(), CHttpRequestUtils.getHeaders(request, "h"));
    }

    // ---------- getAttrStr ----------

    /**
     * 对应测试用例 1.9
     */
    @Test
    void testGetAttrStr_present() {
        CHttpRequest request = Mockito.mock(CHttpRequest.class);
        Mockito.when(request.getAttribute("k")).thenReturn("v");
        Assertions.assertEquals("v", CHttpRequestUtils.getAttrStr(request, "k"));
    }

    /**
     * 对应测试用例 1.10
     */
    @Test
    void testGetAttrStr_nullAttribute_returnsNull() {
        CHttpRequest request = Mockito.mock(CHttpRequest.class);
        Mockito.when(request.getAttribute("k")).thenReturn(null);
        Assertions.assertNull(CHttpRequestUtils.getAttrStr(request, "k"));
    }

    /**
     * 对应测试用例 1.11
     */
    @Test
    void testGetAttrStr_nonStringAttribute() {
        CHttpRequest request = Mockito.mock(CHttpRequest.class);
        Mockito.when(request.getAttribute("k")).thenReturn(123);
        Assertions.assertEquals("123", CHttpRequestUtils.getAttrStr(request, "k"));
    }

    // ---------- getHeaderThenDo ----------

    /**
     * 对应测试用例 1.12：动作集合为空时不执行
     */
    @Test
    void testGetHeaderThenDo_emptyCollection_noAction() {
        AtomicInteger count = new AtomicInteger();
        CHttpRequestUtils.getHeaderThenDo(null, Collections.emptyList(), (k, v) -> count.incrementAndGet());
        Assertions.assertEquals(0, count.get());
    }

    /**
     * 对应测试用例 1.13：动作集合为 null 时不执行
     */
    @Test
    void testGetHeaderThenDo_nullCollection_noAction() {
        AtomicInteger count = new AtomicInteger();
        CHttpRequestUtils.getHeaderThenDo(null, null, (k, v) -> count.incrementAndGet());
        Assertions.assertEquals(0, count.get());
    }

    // ---------- getHeadersThenDo ----------

    /**
     * 对应测试用例 1.14：动作集合为空时不执行
     */
    @Test
    void testGetHeadersThenDo_emptyCollection_noAction() {
        AtomicInteger count = new AtomicInteger();
        CHttpRequestUtils.getHeadersThenDo(null, Collections.emptyList(), (k, v) -> count.incrementAndGet());
        Assertions.assertEquals(0, count.get());
    }

    /**
     * 对应测试用例 1.15：动作集合为 null 时不执行
     */
    @Test
    void testGetHeadersThenDo_nullCollection_noAction() {
        AtomicInteger count = new AtomicInteger();
        CHttpRequestUtils.getHeadersThenDo(null, null, (k, v) -> count.incrementAndGet());
        Assertions.assertEquals(0, count.get());
    }

    // ---------- addPrepare / prepare ----------

    /**
     * 对应测试用例 1.16
     */
    @Test
    void testAddPrepareAndPrepare() {
        AtomicInteger count = new AtomicInteger();
        CBiConsumer<CHttpRequest, CHttpResponse> consumer = (req, res) -> count.incrementAndGet();
        CHttpRequestUtils.addPrepare(consumer);
        CHttpRequestUtils.prepare(null, null);
        CHttpRequestUtils.prepare(null, null);
        Assertions.assertEquals(2, count.get());
    }

    /**
     * 对应测试用例 1.17
     */
    @Test
    void testPrepare_exceptionSwallowed() {
        CBiConsumer<CHttpRequest, CHttpResponse> consumer = (req, res) -> {
            throw new RuntimeException("boom");
        };
        CHttpRequestUtils.addPrepare(consumer);
        Assertions.assertDoesNotThrow(() -> CHttpRequestUtils.prepare(null, null));
    }

    /**
     * 对应测试用例 1.18
     */
    @Test
    void testAddPrepare_nullConsumer_throwsNPE() {
        Assertions.assertThrowsExactly(NullPointerException.class, () -> CHttpRequestUtils.addPrepare(null));
    }

    // ---------- addClear / clear ----------

    /**
     * 对应测试用例 1.19
     */
    @Test
    void testAddClearAndClear() {
        AtomicInteger count = new AtomicInteger();
        CBiConsumer<CHttpRequest, CHttpResponse> consumer = (req, res) -> count.incrementAndGet();
        CHttpRequestUtils.addClear(consumer);
        CHttpRequestUtils.clear(null, null);
        CHttpRequestUtils.clear(null, null);
        Assertions.assertEquals(2, count.get());
    }

    /**
     * 对应测试用例 1.20
     */
    @Test
    void testClear_exceptionSwallowed() {
        CBiConsumer<CHttpRequest, CHttpResponse> consumer = (req, res) -> {
            throw new RuntimeException("boom");
        };
        CHttpRequestUtils.addClear(consumer);
        Assertions.assertDoesNotThrow(() -> CHttpRequestUtils.clear(null, null));
    }

    /**
     * 对应测试用例 1.21
     */
    @Test
    void testAddClear_nullConsumer_throwsNPE() {
        Assertions.assertThrowsExactly(NullPointerException.class, () -> CHttpRequestUtils.addClear(null));
    }

}
