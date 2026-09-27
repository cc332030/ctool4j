package com.c332030.ctool4j.interfaces;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Description: CHttpRequestTests
 * </p>
 *
 * <p>{@code CHttpRequest} 的测试用例</p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code getClientIp()}：接口自带 {@code default} 方法，取客户端 IP</li>
 * </ul>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>只测接口自带的行为（{@code default} 方法），实现侧的纯转发由两侧适配器保证；
 *   用一个最小可控桩实现提供报文头，避开任何 Servlet 类型（本模块不引 servlet-api）。</li>
 *   <li>{@code X-Forwarded-For} 的分支（有该头取首段 / 无该头回落到 {@code getRemoteAddr()}）逐分支覆盖；
 *   首段前后的空白属易漏点，单独成例。</li>
 * </ul>
 *
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：无 {@code X-Forwarded-For} 回落 {@code getRemoteAddr}；有该头取首段；多段取第一段；</li>
 *   <li>首段带空白时按原样返回（决定是否 trim）。</li>
 *   <li>未覆盖：其余同名同签名的纯转发方法——它们直接委托底层容器，没有本层可断言的逻辑。</li>
 * </ul>
 *
 * <h2>用例编号索引</h2>
 * <ul>
 *   <li>1 客户端 IP（1.1-1.4）</li>
 * </ul>
 *
 * @see CHttpRequest
 * @since 2026/9/27
 * @version 1.0
 */
public class CHttpRequestTests {

    /**
     * {@code X-Forwarded-For} 报文头名
     */
    private static final String FORWARDED_FOR = "X-Forwarded-For";

    /**
     * 最小桩实现：只提供报文头与 remoteAddr，其余方法按契约返回"没有"值
     */
    private static class StubRequest implements CHttpRequest {

        private final Map<String, String> headers = new HashMap<>();

        private String remoteAddr;

        @Override
        public Object getAttribute(String name) {
            return null;
        }

        @Override
        public java.util.Enumeration<String> getAttributeNames() {
            return java.util.Collections.emptyEnumeration();
        }

        @Override
        public void setAttribute(String name, Object o) {
            // 本桩无属性语义
        }

        @Override
        public void removeAttribute(String name) {
            // 本桩无属性语义
        }

        @Override
        public String getCharacterEncoding() {
            return null;
        }

        @Override
        public void setCharacterEncoding(String env) {
            // 本桩无编码语义
        }

        @Override
        public int getContentLength() {
            return -1;
        }

        @Override
        public long getContentLengthLong() {
            return -1L;
        }

        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public String getParameter(String name) {
            return null;
        }

        @Override
        public java.util.Enumeration<String> getParameterNames() {
            return java.util.Collections.emptyEnumeration();
        }

        @Override
        public String[] getParameterValues(String name) {
            return null;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return java.util.Collections.emptyMap();
        }

        @Override
        public String getProtocol() {
            return "HTTP/1.1";
        }

        @Override
        public String getScheme() {
            return "http";
        }

        @Override
        public String getServerName() {
            return "localhost";
        }

        @Override
        public int getServerPort() {
            return 80;
        }

        @Override
        public java.io.BufferedReader getReader() {
            return null;
        }

        @Override
        public String getRemoteAddr() {
            return remoteAddr;
        }

        @Override
        public String getRemoteHost() {
            return remoteAddr;
        }

        @Override
        public int getRemotePort() {
            return 0;
        }

        @Override
        public String getLocalName() {
            return "localhost";
        }

        @Override
        public String getLocalAddr() {
            return "127.0.0.1";
        }

        @Override
        public int getLocalPort() {
            return 80;
        }

        @Override
        public java.util.Locale getLocale() {
            return java.util.Locale.getDefault();
        }

        @Override
        public java.util.Enumeration<java.util.Locale> getLocales() {
            return java.util.Collections.emptyEnumeration();
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public CRequestDispatcher getRequestDispatcher(String path) {
            return null;
        }

        @Override
        public boolean isAsyncStarted() {
            return false;
        }

        @Override
        public boolean isAsyncSupported() {
            return false;
        }

        @Override
        public Map<String, String> getTrailerFields() {
            return java.util.Collections.emptyMap();
        }

        @Override
        public boolean isTrailerFieldsReady() {
            return false;
        }

        @Override
        public String getAuthType() {
            return null;
        }

        @Override
        public long getDateHeader(String name) {
            return -1L;
        }

        @Override
        public String getHeader(String name) {
            return headers.get(name);
        }

        @Override
        public java.util.Enumeration<String> getHeaders(String name) {
            val value = headers.get(name);
            return null == value
                ? java.util.Collections.emptyEnumeration()
                : java.util.Collections.enumeration(java.util.Collections.singletonList(value));
        }

        @Override
        public java.util.Enumeration<String> getHeaderNames() {
            return java.util.Collections.enumeration(headers.keySet());
        }

        @Override
        public int getIntHeader(String name) {
            return -1;
        }

        @Override
        public String getMethod() {
            return "GET";
        }

        @Override
        public String getPathInfo() {
            return null;
        }

        @Override
        public String getPathTranslated() {
            return null;
        }

        @Override
        public String getContextPath() {
            return "";
        }

        @Override
        public String getQueryString() {
            return null;
        }

        @Override
        public String getRemoteUser() {
            return null;
        }

        @Override
        public boolean isUserInRole(String role) {
            return false;
        }

        @Override
        public java.security.Principal getUserPrincipal() {
            return null;
        }

        @Override
        public String getRequestedSessionId() {
            return null;
        }

        @Override
        public String getRequestURI() {
            return "/";
        }

        @Override
        public StringBuffer getRequestURL() {
            return new StringBuffer("http://localhost/");
        }

        @Override
        public String getServletPath() {
            return "";
        }

        @Override
        public String changeSessionId() {
            return null;
        }

        @Override
        public boolean isRequestedSessionIdValid() {
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromCookie() {
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromURL() {
            return false;
        }

        @Override
        public ICHttpSession getSession() {
            return null;
        }

        @Override
        public ICHttpSession getSession(boolean create) {
            return null;
        }

        @Override
        public java.util.List<ICCookie> getCookies() {
            return java.util.Collections.emptyList();
        }

        @Override
        public java.io.InputStream getInputStream() {
            return null;
        }

        @Override
        public void login(String username, String password) {
            // 本桩无认证语义
        }

        @Override
        public void logout() {
            // 本桩无认证语义
        }

    }

    /**
     * 客户端 IP：无 X-Forwarded-For 时回落 remoteAddr（对应测试用例 1.1）
     */
    @Test
    public void getClientIp_withoutForwardedFor_fallsBackToRemoteAddr() {

        val request = new StubRequest();
        request.remoteAddr = "10.0.0.1";

        Assertions.assertEquals("10.0.0.1", request.getClientIp());

    }

    /**
     * 客户端 IP：有 X-Forwarded-For 时取首段（对应测试用例 1.2）
     */
    @Test
    public void getClientIp_withForwardedFor_takesFirstSegment() {

        val request = new StubRequest();
        request.remoteAddr = "10.0.0.1";
        request.headers.put(FORWARDED_FOR, "203.0.113.7");

        Assertions.assertEquals("203.0.113.7", request.getClientIp());

    }

    /**
     * 客户端 IP：X-Forwarded-For 多段时只取第一段（对应测试用例 1.3）
     */
    @Test
    public void getClientIp_withMultipleSegments_takesFirstOnly() {

        val request = new StubRequest();
        request.remoteAddr = "10.0.0.1";
        request.headers.put(FORWARDED_FOR, "203.0.113.7, 10.0.0.1, 10.0.0.2");

        Assertions.assertEquals("203.0.113.7", request.getClientIp());

    }

    /**
     * 客户端 IP：X-Forwarded-For 为首段带空白时按原样返回（不 trim）（对应测试用例 1.4）
     */
    @Test
    public void getClientIp_firstSegmentWithSpaces_keptAsIs() {

        val request = new StubRequest();
        request.remoteAddr = "10.0.0.1";
        request.headers.put(FORWARDED_FOR, " 203.0.113.7 , 10.0.0.1");

        Assertions.assertEquals(" 203.0.113.7 ", request.getClientIp());

    }

}
