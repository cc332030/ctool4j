package com.c332030.ctool4j.model;

import com.c332030.ctool4j.interfaces.CHttpResponse;
import com.c332030.ctool4j.interfaces.ICCookie;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * <p>
 * Description: CHttpServletResponse
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CHttpServletResponse} 是 {@link CHttpResponse} 在 <b>javax</b> 侧的落地（适配器）：
 * 持有 {@code javax.servlet.http.HttpServletResponse}，把接口声明的公共方法逐个转发给它。</p>
 * <p>创建入口：{@link #of(HttpServletResponse)}（底层响应由调用方注入）；解包：{@link #unwrap(CHttpResponse)}
 * （供过滤器链与转发器适配器取回底层对象）。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>纯转发、不加工</b>：每个方法只把调用交给底层响应，契约、边界与返回值语义完全沿用
 *   {@link CHttpResponse}（见该接口文档），此处不重复描述、不做兜底。</li>
 *   <li><b>由档位选源目录</b>：本份承 javax 包（{@code src/main/java-javax}），与同模块的 {@code java-jakarta}
 *   一份同包同名、互为镜像；由 JDK 档位选用其一，使用方按自身容器引入对应档位产物。</li>
 *   <li><b>与请求适配器对称</b>：{@link CHttpServletRequest} 与 {@code CHttpServletResponse} 同属 javax 侧落地，
 *   创建方式与转发形态一致。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>无兜底：底层响应的返回值原样返回（未设置的头为 {@code null}、无同名头时集合为空）。</li>
 *   <li>创建时校验底层响应非空（{@link #of(HttpServletResponse)} 的 {@code null} 入参快速失败），
 *   故实例化后的方法调用不会因底层响应缺失而抛 {@link NullPointerException}。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>运行在 javax（Spring Boot 2.x / Servlet 4.0）容器下、需要面向 {@link CHttpResponse} 编写公共代码的场景。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>只覆盖两边公共面，javax 特有的能力（二进制输出流、Cookie、状态码常量）不在此暴露，需要时直接取底层响应。</li>
 *   <li>本类不做线程安全保证：响应是请求级对象，是否可跨线程使用取决于底层实现与容器约定。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.2
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CHttpServletResponse implements CHttpResponse {

    private final HttpServletResponse response;

    /**
     * 以底层 javax 响应创建适配器
     *
     * @param response 底层响应，不可为 null
     * @return 适配器实例
     * @throws NullPointerException 底层响应为 null 时
     */
    public static CHttpServletResponse of(HttpServletResponse response) {
        Objects.requireNonNull(response, "response");
        return new CHttpServletResponse(response);
    }

    /**
     * 取回底层 javax 响应
     *
     * @param response 抽象层响应；可为 null
     * @return 底层 javax 响应；入参为 null 时返回 null
     * @throws IllegalArgumentException 入参不是本模块的适配器实例时
     */
    public static HttpServletResponse unwrap(CHttpResponse response) {

        if (Objects.isNull(response)) {
            return null;
        }
        if (!(response instanceof CHttpServletResponse)) {
            throw new IllegalArgumentException("not a CHttpServletResponse: " + response.getClass().getName());
        }

        return ((CHttpServletResponse) response).response;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStatus(int sc) {
        response.setStatus(sc);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * 本覆写仅存于 javax 侧：Servlet 6.0（jakarta）已移除 {@code setStatus(int, String)}，
     * 故 jakarta 侧沿接口的 {@code default} 降级为仅设状态码；javax 侧容器支持该 API，这里透传状态消息。
     * </p>
     */
    @Override
    public void setStatus(int sc, String sm) {
        response.setStatus(sc, sm);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStatus() {
        return response.getStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHeader(String name, String value) {
        response.setHeader(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addHeader(String name, String value) {
        response.addHeader(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDateHeader(String name, long date) {
        response.setDateHeader(name, date);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDateHeader(String name, long date) {
        response.addDateHeader(name, date);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setIntHeader(String name, int value) {
        response.setIntHeader(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addIntHeader(String name, int value) {
        response.addIntHeader(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsHeader(String name) {
        return response.containsHeader(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHeader(String name) {
        return response.getHeader(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getHeaders(String name) {
        return response.getHeaders(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getHeaderNames() {
        return response.getHeaderNames();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setContentType(String type) {
        response.setContentType(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContentType() {
        return response.getContentType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCharacterEncoding(String charset) {
        response.setCharacterEncoding(charset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCharacterEncoding() {
        return response.getCharacterEncoding();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setContentLength(int len) {
        response.setContentLength(len);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setContentLengthLong(long len) {
        response.setContentLengthLong(len);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrintWriter getWriter() throws IOException {
        return response.getWriter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBufferSize(int size) {
        response.setBufferSize(size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBufferSize() {
        return response.getBufferSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flushBuffer() throws IOException {
        response.flushBuffer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetBuffer() {
        response.resetBuffer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        response.reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCommitted() {
        return response.isCommitted();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLocale(Locale loc) {
        response.setLocale(loc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Locale getLocale() {
        return response.getLocale();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendRedirect(String location) throws IOException {
        response.sendRedirect(location);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendError(int sc, String msg) throws IOException {
        response.sendError(sc, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendError(int sc) throws IOException {
        response.sendError(sc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String encodeURL(String url) {
        return response.encodeURL(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String encodeRedirectURL(String url) {
        return response.encodeRedirectURL(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTrailerFields(Supplier<Map<String, String>> supplier) {
        response.setTrailerFields(supplier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Supplier<Map<String, String>> getTrailerFields() {
        return response.getTrailerFields();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addCookie(ICCookie cookie) {
        if (null == cookie) {
            return;
        }
        val c = new Cookie(cookie.getName(), cookie.getValue());
        if (null != cookie.getPath()) {
            c.setPath(cookie.getPath());
        }
        if (null != cookie.getDomain()) {
            c.setDomain(cookie.getDomain());
        }
        if (null != cookie.getComment()) {
            c.setComment(cookie.getComment());
        }
        c.setMaxAge(cookie.getMaxAge());
        c.setSecure(cookie.isSecure());
        c.setHttpOnly(cookie.isHttpOnly());
        c.setVersion(cookie.getVersion());
        response.addCookie(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        return response.getOutputStream();
    }
}
