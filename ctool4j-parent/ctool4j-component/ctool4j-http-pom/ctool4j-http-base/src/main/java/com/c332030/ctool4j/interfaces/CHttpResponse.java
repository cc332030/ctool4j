package com.c332030.ctool4j.interfaces;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * <p>
 * Description: CHttpResponse
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CHttpResponse} 为 Servlet 响应的<b>抽象层</b>：只声明 {@code javax.servlet.http.HttpServletResponse}
 * 与 {@code jakarta.servlet.http.HttpServletResponse} <b>两边同名同签名的公共方法</b>，
 * 上层代码面向本接口编程，不接触任何一个 Servlet 包。</p>
 * <p>方法按 Servlet 规范分组：</p>
 * <ul>
 *   <li><b>状态</b>：{@code setStatus}（两形态；两参形态为 {@code default}，jakarta 侧已无对应容器 API，
 *   故降级为仅设状态码）/ {@code getStatus}</li>
 *   <li><b>响应头</b>：{@code setHeader} / {@code addHeader} / {@code setDateHeader} / {@code addDateHeader} /
 *   {@code setIntHeader} / {@code addIntHeader} / {@code containsHeader} / {@code getHeader} / {@code getHeaders} /
 *   {@code getHeaderNames}</li>
 *   <li><b>内容类型与编码</b>：{@code setContentType} / {@code getContentType} / {@code setCharacterEncoding} /
 *   {@code getCharacterEncoding} / {@code setContentLength} / {@code setContentLengthLong}</li>
 *   <li><b>输出</b>：{@code getWriter}</li>
 *   <li><b>缓冲与提交</b>：{@code setBufferSize} / {@code getBufferSize} / {@code flushBuffer} / {@code resetBuffer} /
 *   {@code reset} / {@code isCommitted}</li>
 *   <li><b>区域</b>：{@code setLocale} / {@code getLocale}</li>
 *   <li><b>跳转与错误</b>：{@code sendRedirect} / {@code sendError}（两形态）</li>
 *   <li><b>URL 编码</b>：{@code encodeURL} / {@code encodeRedirectURL}</li>
 *   <li><b>trailer</b>：{@code setTrailerFields} / {@code getTrailerFields}</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>只放两边公共面</b>：方法须在 javax 与 jakarta 两套 {@code HttpServletResponse} 中同名、同参数、同返回类型，
 *   且返回类型与抛出异常都不落在 Servlet 包内——故此处只有 {@code String}/{@code int}/{@code long}/{@code boolean}/
 *   {@code Collection}/{@code Locale}/{@code PrintWriter}/{@code Supplier} 这类 JDK 类型。二进制输出以 JDK 的
 *   {@code OutputStream} 暴露 {@code getOutputStream}；写 Cookie 以自有类型 {@link ICCookie} 为入参的 {@code addCookie} 纳入。
 *   已弃用的 {@code encodeUrl}/{@code encodeRedirectUrl} 不进（新增代码不得使用弃用 API）。</li>
 *   <li><b>底层决定用哪个包</b>：本接口不含任何 Servlet 依赖（{@code ctool4j-http-base} 不引 servlet-api）；
 *   由适配模块承接——{@code ctool4j-http-servlet} 按档位选用 {@code javax.servlet} 或 {@code jakarta.servlet}
 *   源码目录，使用方按自身容器依赖引入对应档位产物。</li>
 *   <li><b>接口只声明、不实现</b>：实现是纯转发，契约与语义完全沿用 Servlet 规范，本接口不新增语义、不做兜底。</li>
 *   <li><b>与请求抽象层对称</b>：{@link CHttpRequest} 与 {@code CHttpResponse} 同属该抽象层、同一套取法
 *   （只声明两边公共面、由实现模块选包）。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>本接口不提供兜底：返回值语义与 Servlet 规范一致（未设置的头返回 {@code null}、无同名头时
 *   {@code getHeaders} 返回空集合）；"空则取默认值"由调用方按需处理。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要同时兼容 javax（Spring Boot 2.x / Servlet 4.0）与 jakarta（Spring Boot 3.x / Servlet 5.0+）两套容器的
 *   公共代码、且只用到两边公共方法的场景。</li>
 *   <li>已明确只用某一套容器的场景直接依赖该套 {@code HttpServletResponse} 即可，无需经过本抽象层。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要 {@code ServletOutputStream} 专有能力（{@code isReady}、{@code setWriteListener}）时不适用：{@code getOutputStream} 只以 JDK {@code OutputStream} 暴露。</li>
 *   <li>需要状态码常量（{@code SC_OK} 一类）时同样不在此声明：两侧都是编译期 {@code int} 常量，
 *   本接口只声明方法。</li>
 *   <li>Servlet 6.0 新增方法不进本接口——javax 侧不存在，不属两边公共面。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>公共面以<b>同名同签名</b>为主，差异点按成本决定是否统一：二进制流用 JDK 父类型、Cookie 用自有类型
 *   （{@link ICCookie}）纳入；Session、Part、{@code ServletContext} 等返回类型无法低成本统一，强行包装会让抽象层成为新的兼容负担，暂不做。</li>
 *   <li>抽象层不消除 javax/jakarta 的依赖分裂：两套实现各自声明各自的 servlet-api（均 {@code optional}，
 *   不传递给使用方、运行时由容器提供），引入两个实现模块并不代表可同时在一个应用里生效。</li>
 *   <li>提交语义（{@code isCommitted}/缓冲/重置）的边界完全由底层容器决定，本接口不做任何状态跟踪或拦截。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.2
 */
public interface CHttpResponse {

    /**
     * 设置响应状态码
     *
     * @param sc 状态码
     */
    void setStatus(int sc);

    /**
     * 设置响应状态码与状态消息。
     *
     * <p>
     * 默认实现忽略 {@code sm}、仅设置状态码：Servlet 6.0（jakarta）已移除
     * {@code setStatus(int, String)}，两侧无法给出统一实现；javax 侧适配器覆写本方法以保留状态消息。
     * </p>
     *
     * @param sc 状态码
     * @param sm 状态消息
     */
    default void setStatus(int sc, String sm) {
        setStatus(sc);
    }

    /**
     * 取当前响应状态码
     *
     * @return 状态码；未设置时取容器默认值
     */
    int getStatus();

    /**
     * 设置响应头；同名头以本次设置为准（覆盖已有取值）
     *
     * @param name  响应头名
     * @param value 响应头值
     */
    void setHeader(String name, String value);

    /**
     * 追加响应头；同名头保留已有取值并追加本次
     *
     * @param name  响应头名
     * @param value 响应头值
     */
    void addHeader(String name, String value);

    /**
     * 设置日期响应头；同名头以本次设置为准
     *
     * @param name 响应头名
     * @param date 日期毫秒值
     */
    void setDateHeader(String name, long date);

    /**
     * 追加日期响应头
     *
     * @param name 响应头名
     * @param date 日期毫秒值
     */
    void addDateHeader(String name, long date);

    /**
     * 设置整数响应头；同名头以本次设置为准
     *
     * @param name  响应头名
     * @param value 整数值
     */
    void setIntHeader(String name, int value);

    /**
     * 追加整数响应头
     *
     * @param name  响应头名
     * @param value 整数值
     */
    void addIntHeader(String name, int value);

    /**
     * 判断指定响应头是否已设置
     *
     * @param name 响应头名
     * @return true 表示已设置
     */
    boolean containsHeader(String name);

    /**
     * 取指定响应头的首个取值
     *
     * @param name 响应头名
     * @return 首个取值；未设置时返回 null
     */
    String getHeader(String name);

    /**
     * 取指定响应头的全部取值
     *
     * @param name 响应头名
     * @return 取值集合；未设置时返回空集合
     */
    Collection<String> getHeaders(String name);

    /**
     * 取全部响应头名
     *
     * @return 响应头名集合；无响应头时返回空集合
     */
    Collection<String> getHeaderNames();

    /**
     * 设置响应体 MIME 类型
     *
     * @param type MIME 类型
     */
    void setContentType(String type);

    /**
     * 取响应体 MIME 类型
     *
     * @return MIME 类型；未设置时返回容器默认值
     */
    String getContentType();

    /**
     * 设置响应体字符编码；须在取输出流或响应提交之前调用
     *
     * @param charset 字符编码名
     */
    void setCharacterEncoding(String charset);

    /**
     * 取响应体字符编码
     *
     * @return 字符编码；未设置时返回容器默认值
     */
    String getCharacterEncoding();

    /**
     * 设置响应体长度（字节）
     *
     * @param len 字节数
     */
    void setContentLength(int len);

    /**
     * 设置响应体长度（字节，long 形态）
     *
     * @param len 字节数
     */
    void setContentLengthLong(long len);

    /**
     * 取响应体字符输出流；与二进制输出流互斥，调用其一后另一个不可再用
     *
     * @return 响应体字符输出流
     * @throws IOException 获取输出流失败时
     */
    PrintWriter getWriter() throws IOException;

    /**
     * 设置响应缓冲区大小；须在写入响应体或响应提交之前调用
     *
     * @param size 目标缓冲区大小（字节）
     */
    void setBufferSize(int size);

    /**
     * 取实际生效的响应缓冲区大小
     *
     * @return 缓冲区大小（字节）
     */
    int getBufferSize();

    /**
     * 把缓冲区内容强制写给客户端，并提交响应
     *
     * @throws IOException 写出失败时
     */
    void flushBuffer() throws IOException;

    /**
     * 清空缓冲区内容；响应头与状态码保留，已提交后调用无效
     */
    void resetBuffer();

    /**
     * 清空缓冲区内容、响应头与状态码
     *
     * @throws IllegalStateException 响应已提交时
     */
    void reset();

    /**
     * 判断响应是否已提交
     *
     * @return true 表示已提交（此后不能再改响应头与状态码）
     */
    boolean isCommitted();

    /**
     * 设置响应区域（影响 {@code Content-Language} 等）
     *
     * @param loc 区域
     */
    void setLocale(Locale loc);

    /**
     * 取响应区域
     *
     * @return 区域；未设置时返回容器默认值
     */
    Locale getLocale();

    /**
     * 发送重定向响应；会提交响应并清空缓冲区
     *
     * @param location 目标地址
     * @throws IOException 写出失败时
     */
    void sendRedirect(String location) throws IOException;

    /**
     * 发送错误响应并带错误消息；会提交响应
     *
     * @param sc  状态码
     * @param msg 错误消息
     * @throws IOException 写出失败时
     */
    void sendError(int sc, String msg) throws IOException;

    /**
     * 发送错误响应；会提交响应
     *
     * @param sc 状态码
     * @throws IOException 写出失败时
     */
    void sendError(int sc) throws IOException;

    /**
     * 编码 URL：必要时附加会话 ID（容器不支持 Cookie 时的会话保持手段）
     *
     * @param url 原始 URL
     * @return 编码后的 URL；无需编码时原样返回
     */
    String encodeURL(String url);

    /**
     * 编码供 {@link #sendRedirect} 使用的 URL：必要时附加会话 ID
     *
     * @param url 原始 URL
     * @return 编码后的 URL；无需编码时原样返回
     */
    String encodeRedirectURL(String url);

    /**
     * 设置 trailer 字段提供者（HTTP/1.1 chunked 分块尾部字段）
     *
     * @param supplier trailer 字段提供者
     */
    void setTrailerFields(Supplier<Map<String, String>> supplier);

    /**
     * 取响应体的二进制输出流（底层 Servlet 流的 JDK 父类型）
     *
     * @return 输出流
     * @throws IOException 取流失败，或已被 {@link #getWriter} 占用时
     */
    OutputStream getOutputStream() throws IOException;

    /**
     * 添加 Cookie 到响应
     *
     * @param cookie Cookie
     */
    void addCookie(ICCookie cookie);

    /**
     * 取 trailer 字段提供者
     *
     * @return trailer 字段提供者；未设置时返回 null
     */
    Supplier<Map<String, String>> getTrailerFields();

}
