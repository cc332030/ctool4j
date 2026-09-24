package com.c332030.ctool4j.model;

import com.c332030.ctool4j.exception.CServletException;
import com.c332030.ctool4j.interfaces.CHttpRequest;
import com.c332030.ctool4j.interfaces.CRequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * Description: CHttpServletRequest
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CHttpServletRequest} 是 {@link CHttpRequest} 在 <b>jakarta</b> 侧的落地（适配器）：
 * 持有 {@code jakarta.servlet.http.HttpServletRequest}，把接口声明的公共方法逐个转发给它。</p>
 * <p>创建入口：{@link #of(HttpServletRequest)}（底层请求由调用方注入）；解包：{@link #unwrap(CHttpRequest)}
 * （供过滤器链与转发器适配器取回底层对象）。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>纯转发、不加工</b>：每个方法只把调用交给底层请求，契约、边界与返回值语义完全沿用
 *   {@link CHttpRequest}（见该接口文档），此处不重复描述、不做兜底。</li>
 *   <li><b>由实现模块定包</b>：本模块承 jakarta 包，与其对应的 javax 侧适配器在 {@code ctool4j-http-javax}，
 *   两者实现同一接口、互不依赖，使用方按自身容器选其一。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>无兜底：底层请求的返回值原样返回（未知长度 {@code -1}、查不到 {@code null}）。</li>
 *   <li>创建时校验底层请求非空（{@link #of(HttpServletRequest)} 的 {@code null} 入参快速失败），
 *   故实例化后的方法调用不会因底层请求缺失而抛 {@link NullPointerException}。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>运行在 jakarta（Spring Boot 3.x / Servlet 5.0+）容器下、需要面向 {@link CHttpRequest} 编写公共代码的场景。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>只覆盖两边公共面，jakarta 特有的能力（会话、Cookie、上传分片、{@code authenticate} 发起认证、协议升级）
 *   不在此暴露，需要时直接取底层请求。</li>
 *   <li>本类不做线程安全保证：是否可跨线程使用取决于底层 {@code HttpServletRequest}（容器实现通常只保证同一请求线程内可用）。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.3
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CHttpServletRequest implements CHttpRequest {

    private final HttpServletRequest request;

    /**
     * 以底层 jakarta 请求创建适配器
     *
     * @param request 底层请求，不可为 null
     * @return 适配器实例
     * @throws NullPointerException 底层请求为 null 时
     */
    public static CHttpServletRequest of(HttpServletRequest request) {
        Objects.requireNonNull(request, "request");
        return new CHttpServletRequest(request);
    }

    /**
     * 取回底层 jakarta 请求
     *
     * @param request 抽象层请求；可为 null
     * @return 底层 jakarta 请求；入参为 null 时返回 null
     * @throws IllegalArgumentException 入参不是本模块的适配器实例时
     */
    public static HttpServletRequest unwrap(CHttpRequest request) {

        if (Objects.isNull(request)) {
            return null;
        }
        if (!(request instanceof CHttpServletRequest)) {
            throw new IllegalArgumentException("not a CHttpServletRequest: " + request.getClass().getName());
        }

        return ((CHttpServletRequest) request).request;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getAttribute(String name) {
        return request.getAttribute(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<String> getAttributeNames() {
        return request.getAttributeNames();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(String name, Object o) {
        request.setAttribute(name, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAttribute(String name) {
        request.removeAttribute(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCharacterEncoding() {
        return request.getCharacterEncoding();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        request.setCharacterEncoding(env);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getContentLength() {
        return request.getContentLength();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getContentLengthLong() {
        return request.getContentLengthLong();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContentType() {
        return request.getContentType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParameter(String name) {
        return request.getParameter(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<String> getParameterNames() {
        return request.getParameterNames();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getParameterValues(String name) {
        return request.getParameterValues(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        return request.getParameterMap();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProtocol() {
        return request.getProtocol();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getScheme() {
        return request.getScheme();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServerName() {
        return request.getServerName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getServerPort() {
        return request.getServerPort();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedReader getReader() throws IOException {
        return request.getReader();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRemoteAddr() {
        return request.getRemoteAddr();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRemoteHost() {
        return request.getRemoteHost();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRemotePort() {
        return request.getRemotePort();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLocalName() {
        return request.getLocalName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLocalAddr() {
        return request.getLocalAddr();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLocalPort() {
        return request.getLocalPort();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Locale getLocale() {
        return request.getLocale();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<Locale> getLocales() {
        return request.getLocales();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSecure() {
        return request.isSecure();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CRequestDispatcher getRequestDispatcher(String path) {

        val dispatcher = request.getRequestDispatcher(path);
        if (Objects.isNull(dispatcher)) {
            return null;
        }

        return CRequestDispatcherAdapter.of(dispatcher);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAsyncStarted() {
        return request.isAsyncStarted();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAsyncSupported() {
        return request.isAsyncSupported();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getTrailerFields() {
        return request.getTrailerFields();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTrailerFieldsReady() {
        return request.isTrailerFieldsReady();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthType() {
        return request.getAuthType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDateHeader(String name) {
        return request.getDateHeader(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHeader(String name) {
        return request.getHeader(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<String> getHeaders(String name) {
        return request.getHeaders(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<String> getHeaderNames() {
        return request.getHeaderNames();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIntHeader(String name) {
        return request.getIntHeader(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMethod() {
        return request.getMethod();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPathInfo() {
        return request.getPathInfo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPathTranslated() {
        return request.getPathTranslated();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContextPath() {
        return request.getContextPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getQueryString() {
        return request.getQueryString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRemoteUser() {
        return request.getRemoteUser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUserInRole(String role) {
        return request.isUserInRole(role);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Principal getUserPrincipal() {
        return request.getUserPrincipal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequestedSessionId() {
        return request.getRequestedSessionId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequestURI() {
        return request.getRequestURI();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringBuffer getRequestURL() {
        return request.getRequestURL();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServletPath() {
        return request.getServletPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String changeSessionId() {
        return request.changeSessionId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRequestedSessionIdValid() {
        return request.isRequestedSessionIdValid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return request.isRequestedSessionIdFromCookie();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRequestedSessionIdFromURL() {
        return request.isRequestedSessionIdFromURL();
    }

    /**
     * 以容器表单认证方式登录；底层 {@code ServletException} 包装为 {@link CServletException} 抛出
     */
    @Override
    public void login(String username, String password) {
        try {
            request.login(username, password);
        } catch (ServletException e) {
            throw new CServletException(e);
        }
    }

    /**
     * 注销当前认证；底层 {@code ServletException} 包装为 {@link CServletException} 抛出
     */
    @Override
    public void logout() {
        try {
            request.logout();
        } catch (ServletException e) {
            throw new CServletException(e);
        }
    }

}
