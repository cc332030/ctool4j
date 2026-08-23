package com.c332030.ctool4j.web.test.util;

import com.c332030.ctool4j.web.util.CAuthUtils;
import lombok.CustomLog;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * <p>
 * Description: CAuthUtilsTests
 * </p>
 * <p>`com.c332030.ctool4j.web.util.CAuthUtils`（CAuthUtils）的测试用例</p>
 *
 * <p>覆盖 token 前缀移除、请求头取 token、响应头写 token 等纯逻辑方法；
 * 依赖 Spring 容器的方法（无参 getToken/setToken 等）不在本测试覆盖范围</p>
 *
 * @since 2026/8/14
 */
@CustomLog
public class CAuthUtilsTests {

    // ---------- removePrefix ----------

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void removePrefix_normal() {
        // 正例：移除 Bearer 前缀并跳过 1 个空格
        Assertions.assertEquals("abc123", CAuthUtils.removePrefix("Bearer abc123"));
    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void removePrefix_blank_returnsNull() {
        // 边界：null/空/纯空白返回 null
        Assertions.assertNull(CAuthUtils.removePrefix(null));
        Assertions.assertNull(CAuthUtils.removePrefix(""));
        Assertions.assertNull(CAuthUtils.removePrefix("   "));
    }

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void removePrefix_equalsPrefix() {
        // 边界：token 恰好等于 "Bearer"（长度不大于前缀）返回原串
        Assertions.assertEquals("Bearer", CAuthUtils.removePrefix("Bearer"));
    }

    /**
     * 对应测试用例 1.4
     */
    @Test
    public void removePrefix_shorterThanPrefix() {
        // 边界：token 长度小于前缀返回原串
        Assertions.assertEquals("Bea", CAuthUtils.removePrefix("Bea"));
    }

    /**
     * 对应测试用例 1.5
     */
    @Test
    public void removePrefix_noPrefix() {
        // 反例：不以 Bearer 开头返回原串
        Assertions.assertEquals("token-xyz", CAuthUtils.removePrefix("token-xyz"));
    }

    /**
     * 对应测试用例 1.6
     */
    @Test
    public void removePrefix_bearerNoSpace() {
        // 边界：Bearer 后无空格时按 prefix+1 截断（substring(prefix.length()+1)）
        Assertions.assertEquals("bc", CAuthUtils.removePrefix("Bearerabc"));
    }

    /**
     * 对应测试用例 1.7
     */
    @Test
    public void removePrefix_caseSensitive() {
        // 反例：小写 bearer 不以大写 Bearer 开头，返回原串（区分大小写）
        Assertions.assertEquals("bearer abc", CAuthUtils.removePrefix("bearer abc"));
    }

    // ---------- getToken(HttpServletRequest, String) ----------

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void getToken_requestPrefix() {
        // 正例：从请求头解析 token
        val request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token-1");
        Assertions.assertEquals("token-1", CAuthUtils.getToken(request, "Bearer"));
    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void getToken_noAuthorization() {
        // 边界：无 Authorization 头返回 null
        val request = new MockHttpServletRequest();
        Assertions.assertNull(CAuthUtils.getToken(request, "Bearer"));
    }

    /**
     * 对应测试用例 2.3
     */
    @Test
    public void getToken_emptyPrefix() {
        // 反例：前缀不匹配返回 null
        val request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic xxxx");
        Assertions.assertNull(CAuthUtils.getToken(request, "Bearer"));
    }

    /**
     * 对应测试用例 2.4
     */
    @Test
    public void getToken_lengthEqualsPrefix() {
        // 边界：authorization 长度等于前缀时不返回（<= prefix.length()）
        val request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer");
        Assertions.assertNull(CAuthUtils.getToken(request, "Bearer"));
    }

    /**
     * 对应测试用例 2.5
     */
    @Test
    public void getToken_nullRequest_returnsNull() {
        // 边界：null request 按无请求处理，返回 null
        Assertions.assertNull(CAuthUtils.getToken(null, "Bearer"));
    }

    /**
     * 对应测试用例 2.6
     */
    @Test
    public void getToken_customPrefix() {
        // 正例：自定义前缀解析
        val request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Custom token-2");
        Assertions.assertEquals("token-2", CAuthUtils.getToken(request, "Custom"));
    }

    // ---------- setToken(String, String, HttpServletResponse) ----------

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void setToken_response() {
        // 正例：设置 Authorization 响应头
        val response = new MockHttpServletResponse();
        CAuthUtils.setToken("token-3", "Bearer", response);
        Assertions.assertEquals("Bearer token-3", response.getHeader(HttpHeaders.AUTHORIZATION));
    }

    /**
     * 对应测试用例 3.2
     */
    @Test
    public void setToken_customPrefix() {
        // 正例：自定义前缀
        val response = new MockHttpServletResponse();
        CAuthUtils.setToken("token-4", "Custom", response);
        Assertions.assertEquals("Custom token-4", response.getHeader(HttpHeaders.AUTHORIZATION));
    }

    /**
     * 对应测试用例 3.3
     */
    @Test
    public void setToken_nullResponse() {
        // 异常路径：null response 抛出 NPE
        Assertions.assertThrowsExactly(
            NullPointerException.class,
            () -> CAuthUtils.setToken("t", "Bearer", null)
        );
    }

}
