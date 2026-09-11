package com.c332030.ctool4j.web.test.util;

import com.c332030.ctool4j.web.util.CTokenUtils;
import lombok.CustomLog;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * <p>
 * Description: CTokenUtilsTests
 * </p>
 * <p>`com.c332030.ctool4j.web.util.CTokenUtils`（CTokenUtils）的测试用例</p>
 *
 * <p>覆盖 token 前缀移除、请求头取 token、响应头写 token，以及请求属性 token 读写等纯逻辑方法。</p>
 *
 * <p><b>用例设计思路</b>：按「removePrefix / getHeaderToken / setHeaderToken / 请求属性」多个维度组织，
 * 覆盖各空值/前缀匹配边界，并区分「显式传入 request/response 的重载」与「依赖当前请求上下文的上下文重载」两类。</p>
 * <p><b>设计依据</b>：依据功能设计对前缀匹配、空值兜底的约定；上下文重载以 Spring 的
 * {@code RequestContextHolder} 绑定请求/响应后验证，非请求环境按约定抛 {@link IllegalArgumentException}。</p>
 * <p><b>覆盖场景</b>：removePrefix 正常/空白/等于前缀/短于前缀/无前缀/无空格/仅前缀加空格/大小写；getHeaderToken 请求头各形态/
 * 自定义前缀/null request/上下文重载（无参/带前缀）；setHeaderToken 默认前缀/自定义前缀/null response/上下文重载/两参重载；
 * 请求属性 setToken（显式/上下文）/getToken/getTokenOrNew/非请求环境抛异常。</p>
 * <p><b>未覆盖</b>：无。</p>
 *
 * <p><b>用例编号索引</b>：1 removePrefix（1.1-1.8）；2 getHeaderToken（2.1-2.8）；3 setHeaderToken（3.1-3.6）；
 * 4 请求属性（4.1-4.5）。各测试方法 javadoc 标注其编号与说明。</p>
 *
 * @since 2026/8/14
 */
@CustomLog
public class CTokenUtilsTests {

    /**
     * 清理请求上下文：上下文重载用例会绑定请求/响应，避免状态残留污染同 JVM 的其他用例
     */
    @AfterEach
    public void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    // ---------- removePrefix ----------

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void removePrefix_normal() {
        // 正例：移除 Bearer 前缀并跳过 1 个空格
        Assertions.assertEquals("abc123", CTokenUtils.removePrefix("Bearer abc123"));
    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void removePrefix_blank_returnsNull() {
        // 边界：null/空/纯空白返回 null
        Assertions.assertNull(CTokenUtils.removePrefix(null));
        Assertions.assertNull(CTokenUtils.removePrefix(""));
        Assertions.assertNull(CTokenUtils.removePrefix("   "));
    }

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void removePrefix_equalsPrefix() {
        // 边界：token 恰好等于 "Bearer"（长度不大于前缀）返回原串
        Assertions.assertEquals("Bearer", CTokenUtils.removePrefix("Bearer"));
    }

    /**
     * 对应测试用例 1.4
     */
    @Test
    public void removePrefix_shorterThanPrefix() {
        // 边界：token 长度小于前缀返回原串
        Assertions.assertEquals("Bea", CTokenUtils.removePrefix("Bea"));
    }

    /**
     * 对应测试用例 1.5
     */
    @Test
    public void removePrefix_noPrefix() {
        // 反例：不以 Bearer 开头返回原串
        Assertions.assertEquals("token-xyz", CTokenUtils.removePrefix("token-xyz"));
    }

    /**
     * 对应测试用例 1.6
     */
    @Test
    public void removePrefix_bearerNoSpace() {
        // 边界：Bearer 后无空格时按 prefix+1 截断（substring(prefix.length()+1)）
        Assertions.assertEquals("bc", CTokenUtils.removePrefix("Bearerabc"));
    }

    /**
     * 对应测试用例 1.7
     */
    @Test
    public void removePrefix_caseSensitive() {
        // 反例：小写 bearer 不以大写 Bearer 开头，返回原串（区分大小写）
        Assertions.assertEquals("bearer abc", CTokenUtils.removePrefix("bearer abc"));
    }

    /**
     * 对应测试用例 1.8
     */
    @Test
    public void removePrefix_prefixWithSpaceOnly_returnsEmpty() {
        // 边界：仅 "Bearer "（前缀 + 空格）时按 prefix.length()+1 截断，得到空串（非 null）
        Assertions.assertEquals("", CTokenUtils.removePrefix("Bearer "));
    }

    // ---------- getHeaderToken(HttpServletRequest, String) ----------

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void getHeaderToken_requestPrefix() {
        // 正例：从请求头解析 token
        val request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token-1");
        Assertions.assertEquals("token-1", CTokenUtils.getHeaderToken(request, "Bearer"));
    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void getHeaderToken_noAuthorization() {
        // 边界：无 Authorization 头返回 null
        val request = new MockHttpServletRequest();
        Assertions.assertNull(CTokenUtils.getHeaderToken(request, "Bearer"));
    }

    /**
     * 对应测试用例 2.3
     */
    @Test
    public void getHeaderToken_emptyPrefix() {
        // 反例：前缀不匹配返回 null
        val request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic xxxx");
        Assertions.assertNull(CTokenUtils.getHeaderToken(request, "Bearer"));
    }

    /**
     * 对应测试用例 2.4
     */
    @Test
    public void getHeaderToken_lengthEqualsPrefix() {
        // 边界：authorization 长度等于前缀时不返回（<= prefix.length()）
        val request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer");
        Assertions.assertNull(CTokenUtils.getHeaderToken(request, "Bearer"));
    }

    /**
     * 对应测试用例 2.5
     */
    @Test
    public void getHeaderToken_nullRequest_returnsNull() {
        // 边界：null request 按无请求处理，返回 null
        Assertions.assertNull(CTokenUtils.getHeaderToken(null, "Bearer"));
    }

    /**
     * 对应测试用例 2.6
     */
    @Test
    public void getHeaderToken_customPrefix() {
        // 正例：自定义前缀解析
        val request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Custom token-2");
        Assertions.assertEquals("token-2", CTokenUtils.getHeaderToken(request, "Custom"));
    }

    /**
     * 对应测试用例 2.7
     */
    @Test
    public void getHeaderToken_defaultsToCurrentRequest() {
        // 正例：无参重载取当前请求上下文（等价于 getHeaderToken(TOKEN_PREFIX)）
        val request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token-7");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Assertions.assertEquals("token-7", CTokenUtils.getHeaderToken());
    }

    /**
     * 对应测试用例 2.8
     */
    @Test
    public void getHeaderToken_prefixUsesCurrentRequest() {
        // 正例：带前缀重载取当前请求上下文
        val request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Custom token-8");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Assertions.assertEquals("token-8", CTokenUtils.getHeaderToken("Custom"));
    }

    // ---------- setHeaderToken(String, String, HttpServletResponse) ----------

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void setHeaderToken_response() {
        // 正例：设置 Authorization 响应头
        val response = new MockHttpServletResponse();
        CTokenUtils.setHeaderToken("token-3", "Bearer", response);
        Assertions.assertEquals("Bearer token-3", response.getHeader(HttpHeaders.AUTHORIZATION));
    }

    /**
     * 对应测试用例 3.2
     */
    @Test
    public void setHeaderToken_customPrefix() {
        // 正例：自定义前缀
        val response = new MockHttpServletResponse();
        CTokenUtils.setHeaderToken("token-4", "Custom", response);
        Assertions.assertEquals("Custom token-4", response.getHeader(HttpHeaders.AUTHORIZATION));
    }

    /**
     * 对应测试用例 3.3
     */
    @Test
    public void setHeaderToken_nullResponse() {
        // 异常路径：null response 抛出 NPE
        Assertions.assertThrowsExactly(
            NullPointerException.class,
            () -> CTokenUtils.setHeaderToken("t", "Bearer", null)
        );
    }

    /**
     * 对应测试用例 3.4
     */
    @Test
    public void setHeaderToken_defaultsToCurrentResponse() {
        // 正例：单参重载写入当前响应，前缀取默认 Bearer
        val response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(new MockHttpServletRequest(), response)
        );

        CTokenUtils.setHeaderToken("token-5");
        Assertions.assertEquals("Bearer token-5", response.getHeader(HttpHeaders.AUTHORIZATION));
    }

    /**
     * 对应测试用例 3.5
     */
    @Test
    public void setHeaderToken_customPrefixUsesCurrentResponse() {
        // 正例：带前缀重载写入当前响应
        val response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(new MockHttpServletRequest(), response)
        );

        CTokenUtils.setHeaderToken("token-6", "Custom");
        Assertions.assertEquals("Custom token-6", response.getHeader(HttpHeaders.AUTHORIZATION));
    }

    /**
     * 对应测试用例 3.6
     */
    @Test
    public void setHeaderToken_twoArgUsesDefaultPrefix() {
        // 正例：两参重载（token + response）前缀取默认 Bearer
        val response = new MockHttpServletResponse();
        CTokenUtils.setHeaderToken("token-7", response);
        Assertions.assertEquals("Bearer token-7", response.getHeader(HttpHeaders.AUTHORIZATION));
    }

    // ---------- 请求属性 ----------

    /**
     * 对应测试用例 4.1
     */
    @Test
    public void setToken_and_getToken_fromRequestAttribute() {
        // 正例：写入/读取当前请求属性中的 token
        val request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        CTokenUtils.setToken(request, "attr-token");
        Assertions.assertEquals("attr-token", CTokenUtils.getToken());
    }

    /**
     * 对应测试用例 4.2
     */
    @Test
    public void getTokenOrNew_generatesWhenAbsent() {
        // 边界：属性不存在时生成新 token
        val request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Assertions.assertNotNull(CTokenUtils.getTokenOrNew());
    }

    /**
     * 对应测试用例 4.3
     */
    @Test
    public void getTokenOrNew_returnsExisting() {
        // 正例：属性存在时返回已有 token
        val request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        CTokenUtils.setToken(request, "existing");
        Assertions.assertEquals("existing", CTokenUtils.getTokenOrNew());
    }

    /**
     * 对应测试用例 4.4
     */
    @Test
    public void setToken_writesCurrentRequest() {
        // 正例：单参重载将 token 写入当前请求属性
        val request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        CTokenUtils.setToken("ctx-token");
        Assertions.assertEquals("ctx-token", CTokenUtils.getToken());
    }

    /**
     * 对应测试用例 4.5
     */
    @Test
    public void getToken_withoutRequestContext_throws() {
        // 异常路径：非请求环境取不到请求，CRequestUtils 抛 IllegalArgumentException
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> CTokenUtils.getToken());
    }

}
