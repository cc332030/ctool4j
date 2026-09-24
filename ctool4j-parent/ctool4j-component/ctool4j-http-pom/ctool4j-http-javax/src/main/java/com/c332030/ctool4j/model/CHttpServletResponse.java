package com.c332030.ctool4j.model;

import com.c332030.ctool4j.interfaces.CHttpResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
 *   <li><b>由实现模块定包</b>：本模块承 javax 包，与其对应的 jakarta 侧适配器在 {@code ctool4j-http-jakarta}，
 *   两者实现同一接口、互不依赖，使用方按自身容器选其一。</li>
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

}
