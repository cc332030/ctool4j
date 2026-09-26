package com.c332030.ctool4j.interfaces;

import com.c332030.ctool4j.exception.CServletException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * Description: CHttpRequest
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CHttpRequest} 为 Servlet 请求的<b>抽象层</b>：只声明 {@code javax.servlet.http.HttpServletRequest}
 * 与 {@code jakarta.servlet.http.HttpServletRequest} <b>两边同名同签名的公共方法</b>，
 * 上层代码面向本接口编程，不接触任何一个 Servlet 包。</p>
 * <p>方法按 Servlet 规范分组：</p>
 * <ul>
 *   <li><b>属性</b>：{@code getAttribute} / {@code getAttributeNames} / {@code setAttribute} / {@code removeAttribute}</li>
 *   <li><b>参数</b>：{@code getParameter} / {@code getParameterNames} / {@code getParameterValues} / {@code getParameterMap}</li>
 *   <li><b>编码与请求体</b>：{@code getCharacterEncoding} / {@code setCharacterEncoding} /
 *   {@code getContentLength} / {@code getContentLengthLong} / {@code getContentType} / {@code getReader}</li>
 *   <li><b>请求行与路径</b>：{@code getMethod} / {@code getProtocol} / {@code getScheme} / {@code getRequestURI} /
 *   {@code getRequestURL} / {@code getContextPath} / {@code getServletPath} / {@code getPathInfo} /
 *   {@code getPathTranslated} / {@code getQueryString}</li>
 *   <li><b>报文头</b>：{@code getHeader} / {@code getHeaders} / {@code getHeaderNames} / {@code getDateHeader} /
 *   {@code getIntHeader}</li>
 *   <li><b>服务端与客户端地址</b>：{@code getServerName} / {@code getServerPort} / {@code getRemoteAddr} /
 *   {@code getRemoteHost} / {@code getRemotePort} / {@code getLocalName} / {@code getLocalAddr} /
 *   {@code getLocalPort} / {@code isSecure}</li>
 *   <li><b>请求分派</b>：{@code getRequestDispatcher}（返回本抽象层的 {@link CRequestDispatcher}）</li>
 *   <li><b>区域</b>：{@code getLocale} / {@code getLocales}</li>
 *   <li><b>认证</b>：{@code getAuthType} / {@code getRemoteUser} / {@code isUserInRole} / {@code getUserPrincipal} /
 *   {@code login} / {@code logout}</li>
 *   <li><b>会话标识</b>：{@code getRequestedSessionId} / {@code changeSessionId} /
 *   {@code isRequestedSessionIdValid} / {@code isRequestedSessionIdFromCookie} / {@code isRequestedSessionIdFromURL}</li>
 *   <li><b>异步与 trailer</b>：{@code isAsyncStarted} / {@code isAsyncSupported} / {@code getTrailerFields} /
 *   {@code isTrailerFieldsReady}</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>只放两边公共面</b>：方法须在 javax 与 jakarta 两套 {@code HttpServletRequest} 中同名、同参数、同返回类型，
 *   且返回类型与抛出异常都不落在 Servlet 包内——故此处只有 {@code String}/{@code int}/{@code long}/{@code boolean}/
 *   {@code Enumeration}/{@code Map}/{@code Locale}/{@code Principal} 这类 JDK 类型，以及本抽象层的
 *   {@link CRequestDispatcher}。返回 Servlet 类型的 {@code getCookies}、{@code getSession} 已以自有类型（{@link ICCookie}、{@link ICHttpSession}）统一<b>纳入</b>；
 *   {@code getParts}、{@code getServletContext}、{@code getPart}、{@code upgrade} 留待同类包装、暂不纳入（声明在一个方法上必然偏向其中一边）；
 *   抛 Servlet 异常的 {@code login}/{@code logout} 经 {@link CServletException} 包装后<b>已纳入</b>本接口。</li>
 *   <li><b>底层决定用哪个包</b>：本接口不含任何 Servlet 依赖（{@code ctool4j-http-base} 不引 servlet-api）；
 *   由适配模块承接——{@code ctool4j-http-servlet} 按档位选用 {@code javax.servlet} 或 {@code jakarta.servlet}
 *   源码目录，使用方按自身容器依赖引入对应档位产物。</li>
 *   <li><b>接口只声明、不实现</b>：实现是纯转发，契约与语义完全沿用 Servlet 规范，本接口不新增语义、
 *   不做兜底（如 {@code getParameter} 查不到即按规范返回 {@code null}）。</li>
 *   <li><b>跨包异常包装</b>：底层 Servlet 异常（{@code javax.servlet.ServletException} 与
 *   {@code jakarta.servlet.ServletException}）是两套包里的不同类、无法出现在同一方法签名上，
 *   故由适配器捕获后包装为 {@link CServletException}——抽象层只暴露自有异常类型，使用方不接触任何 Servlet 包。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>本接口不提供兜底：返回值语义与 Servlet 规范一致（未知长度返回 {@code -1}、查不到返回 {@code null}、
 *   无名字的集合返回空 {@code Enumeration}）；需要"空则取默认值"由调用方按需处理。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要同时兼容 javax（Spring Boot 2.x / Servlet 4.0）与 jakarta（Spring Boot 3.x / Servlet 5.0+）两套容器的
 *   公共代码、且只用到两边公共方法的场景。</li>
 *   <li>已明确只用某一套容器的场景直接依赖该套 {@code HttpServletRequest} 即可，无需经过本抽象层。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要 {@code getParts}/{@code getPart}（取 multipart 表单部分，<b>非</b>文件分片）、{@code authenticate}（发起容器认证，
 *   入参为 Servlet 响应类型）、{@code upgrade}（协议升级，返回 Servlet 类型）时：返回类型或入参落在 Servlet 包内，
 *   本接口表达不了，须由实现侧直接使用对应包的类型——{@code login}/{@code logout} 不在此列，其底层异常已包装为 {@link CServletException}。</li>
 *   <li>二进制读取已提供 {@code getInputStream}（底层 {@code ServletInputStream} 以 JDK {@code InputStream} 暴露，不暴露 Servlet 专有方法）；与字符流 {@code getReader} 二选一。</li>
 *   <li>Servlet 6.0 新增方法（{@code getRequestId}、{@code getServletConnection} 等）不进本接口——javax 侧不存在，
 *   不属两边公共面。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>公共面只覆盖两边<b>同名同签名</b>的方法：两边真正的差异点（会话、Cookie、上传分片、协议升级）返回或入参
 *   落在 Servlet 包内，无法直接声明——改以<b>自有包装类型</b>统一（如 {@link ICCookie}），使用方仍零 Servlet 依赖；
 *   跨包异常（{@link CServletException}）与 Cookie（{@link ICCookie}）是"两侧类型不同"里能低成本包装的一类。</li>
 *   <li>抽象层不消除 javax/jakarta 的依赖分裂：两套实现各自声明各自的 servlet-api（均 {@code optional}，
 *   不传递给使用方、运行时由容器提供），引入两个实现模块并不代表可同时在一个应用里生效。</li>
 *   <li>{@code login}/{@code logout} 的失败在本接口是运行时异常（见 {@link CServletException}）：
 *   不像底层 API 那样由编译器强制处理，失败可见性依赖调用方按各方法文档自行处理。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.4
 */
public interface CHttpRequest {

    /**
     * 取请求属性值
     *
     * @param name 属性名
     * @return 属性值；不存在时返回 null
     */
    Object getAttribute(String name);

    /**
     * 取全部请求属性名
     *
     * @return 属性名枚举；无属性时返回空枚举
     */
    Enumeration<String> getAttributeNames();

    /**
     * 设置请求属性；同名属性以本次设置为准
     *
     * @param name 属性名
     * @param o    属性值
     */
    void setAttribute(String name, Object o);

    /**
     * 移除请求属性
     *
     * @param name 属性名
     */
    void removeAttribute(String name);

    /**
     * 取请求字符编码
     *
     * @return 字符编码；未指定时返回 null
     */
    String getCharacterEncoding();

    /**
     * 设置请求字符编码；须在读取参数之前调用，否则不生效
     *
     * @param env 字符编码名
     * @throws UnsupportedEncodingException 编码名不受支持时
     */
    void setCharacterEncoding(String env) throws UnsupportedEncodingException;

    /**
     * 取请求体长度（字节）
     *
     * @return 请求体长度；未知时返回 -1
     */
    int getContentLength();

    /**
     * 取请求体长度（字节，long 形态）
     *
     * @return 请求体长度；未知时返回 -1
     */
    long getContentLengthLong();

    /**
     * 取请求体 MIME 类型
     *
     * @return MIME 类型；未知时返回 null
     */
    String getContentType();

    /**
     * 取请求参数值（查询串与表单体）
     *
     * @param name 参数名
     * @return 首个参数值；不存在时返回 null
     */
    String getParameter(String name);

    /**
     * 取全部请求参数名
     *
     * @return 参数名枚举；无参数时返回空枚举
     */
    Enumeration<String> getParameterNames();

    /**
     * 取同名请求参数的全部取值
     *
     * @param name 参数名
     * @return 参数值数组；不存在时返回 null
     */
    String[] getParameterValues(String name);

    /**
     * 取全部请求参数
     *
     * @return 参数名到取值数组的映射；只读，无参数时返回空 Map
     */
    Map<String, String[]> getParameterMap();

    /**
     * 取请求行协议名与版本
     *
     * @return 协议名与版本（如 {@code HTTP/1.1}）
     */
    String getProtocol();

    /**
     * 取请求协议 scheme
     *
     * @return scheme（如 {@code http}、{@code https}）
     */
    String getScheme();

    /**
     * 取请求到达的服务端主机名
     *
     * @return 服务端主机名
     */
    String getServerName();

    /**
     * 取请求到达的服务端端口
     *
     * @return 服务端端口
     */
    int getServerPort();

    /**
     * 取请求体字符流；与 {@code getInputStream} 二选一，调用其一后另一个不可再用
     *
     * @return 请求体字符流
     * @throws IOException 读取请求体失败时
     */
    BufferedReader getReader() throws IOException;

    /**
     * 取发起请求的客户端 IP
     *
     * @return 客户端 IP
     */
    String getRemoteAddr();

    /**
     * 客户端 IP：优先取 {@code X-Forwarded-For} 首段，无该头时取 {@link #getRemoteAddr()}
     *
     * <p><b>安全取舍</b>：无条件信任 {@code X-Forwarded-For} 首段，客户端直连时可伪造该头绕过 IP 校验；
     * 对外场景须由可信代理清理/覆盖该头。</p>
     *
     * @return 客户端 IP
     */
    default String getClientIp() {
        String forwarded = getHeader("X-Forwarded-For");
        if (null != forwarded && !forwarded.isEmpty()) {
            return forwarded.split(",")[0];
        }
        return getRemoteAddr();
    }

    /**
     * 取发起请求的客户端主机名
     *
     * @return 客户端主机名；无法反查时返回 IP
     */
    String getRemoteHost();

    /**
     * 取请求来源的客户端端口
     *
     * @return 客户端端口
     */
    int getRemotePort();

    /**
     * 取接收请求的本机主机名
     *
     * @return 本机主机名
     */
    String getLocalName();

    /**
     * 取接收请求的本机 IP
     *
     * @return 本机 IP
     */
    String getLocalAddr();

    /**
     * 取接收请求的本机端口
     *
     * @return 本机端口
     */
    int getLocalPort();

    /**
     * 取客户端首选区域
     *
     * @return 首选区域；无 {@code Accept-Language} 时取服务端默认区域
     */
    Locale getLocale();

    /**
     * 取客户端可接受的全部区域，按优先级排列
     *
     * @return 区域枚举
     */
    Enumeration<Locale> getLocales();

    /**
     * 判断请求是否经安全通道（如 HTTPS）到达
     *
     * @return true 表示安全通道
     */
    boolean isSecure();

    /**
     * 取指定路径的请求转发器
     *
     * @param path 目标资源路径（相对当前上下文）
     * @return 转发器；路径为空或容器无法为该路径创建转发器时返回 null
     */
    CRequestDispatcher getRequestDispatcher(String path);

    /**
     * 判断是否已进入异步模式
     *
     * @return true 表示已调用过 startAsync
     */
    boolean isAsyncStarted();

    /**
     * 判断本请求是否支持异步处理
     *
     * @return true 表示支持异步
     */
    boolean isAsyncSupported();

    /**
     * 取请求的 trailer 字段（HTTP/1.1 chunked 分块尾部字段）
     *
     * @return trailer 字段映射；无 trailer 时返回空 Map
     */
    Map<String, String> getTrailerFields();

    /**
     * 判断 trailer 字段是否已就绪
     *
     * @return true 表示 trailer 字段已可读
     */
    boolean isTrailerFieldsReady();

    /**
     * 取容器认证方式
     *
     * @return 认证方式名（如 {@code BASIC}）；未认证时返回 null
     */
    String getAuthType();

    /**
     * 取指定报文头的日期值
     *
     * @param name 报文头名
     * @return 日期毫秒值；头不存在时返回 -1
     * @throws IllegalArgumentException 头存在但取值不是合法日期时
     */
    long getDateHeader(String name);

    /**
     * 取指定报文头的值
     *
     * @param name 报文头名
     * @return 首个取值；头不存在时返回 null
     */
    String getHeader(String name);

    /**
     * 取同名报文头的全部取值
     *
     * @param name 报文头名
     * @return 取值枚举；头不存在时返回空枚举
     */
    Enumeration<String> getHeaders(String name);

    /**
     * 取全部报文头名
     *
     * @return 报文头名枚举；无报文头时返回空枚举
     */
    Enumeration<String> getHeaderNames();

    /**
     * 取指定报文头的整数值
     *
     * @param name 报文头名
     * @return 整数值；头不存在时返回 -1
     * @throws NumberFormatException 头存在但取值不是整数时
     */
    int getIntHeader(String name);

    /**
     * 取请求方法
     *
     * @return 请求方法（如 {@code GET}、{@code POST}）
     */
    String getMethod();

    /**
     * 取请求 URI 中排除 Servlet 路径与路径参数后的额外路径
     *
     * @return 额外路径；不存在时返回 null
     */
    String getPathInfo();

    /**
     * 取 {@link #getPathInfo()} 经容器映射后的真实文件系统路径
     *
     * @return 真实路径；无法映射时返回 null
     */
    String getPathTranslated();

    /**
     * 取请求上下文路径
     *
     * @return 上下文路径；根上下文返回空串
     */
    String getContextPath();

    /**
     * 取请求行中的查询串
     *
     * @return 查询串；无查询串时返回 null
     */
    String getQueryString();

    /**
     * 取容器认证得到的用户名
     *
     * @return 用户名；未认证时返回 null
     */
    String getRemoteUser();

    /**
     * 判断已认证用户是否属于指定角色
     *
     * @param role 角色名
     * @return true 表示属于该角色；未认证时返回 false
     */
    boolean isUserInRole(String role);

    /**
     * 取容器认证得到的用户主体
     *
     * @return 用户主体；未认证时返回 null
     */
    Principal getUserPrincipal();

    /**
     * 取请求携带的会话 ID
     *
     * @return 会话 ID；请求未携带时返回 null
     */
    String getRequestedSessionId();

    /**
     * 取请求 URI（不含查询串）
     *
     * @return 请求 URI
     */
    String getRequestURI();

    /**
     * 取请求 URL（含协议、主机、端口与路径，不含查询串）
     *
     * @return 请求 URL
     */
    StringBuffer getRequestURL();

    /**
     * 取当前 Servlet 映射的路径
     *
     * @return Servlet 路径；根映射返回空串
     */
    String getServletPath();

    /**
     * 更换会话 ID 并返回新 ID
     *
     * @return 新的会话 ID
     * @throws IllegalStateException 请求未关联会话时
     */
    String changeSessionId();

    /**
     * 判断请求携带的会话 ID 是否仍然有效
     *
     * @return true 表示有效
     */
    boolean isRequestedSessionIdValid();

    /**
     * 判断请求携带的会话 ID 是否来自 Cookie
     *
     * @return true 表示来自 Cookie
     */
    boolean isRequestedSessionIdFromCookie();

    /**
     * 判断请求携带的会话 ID 是否来自 URL 重写
     *
     * @return true 表示来自 URL
     */
    boolean isRequestedSessionIdFromURL();

    /**
     * 取请求携带的 Cookie 列表
     *
     * @return Cookie 列表；未携带任何 Cookie 时返回空列表
     */
    /**
     * 取会话；不存在时创建
     *
     * @return 会话
     */
    ICHttpSession getSession();

    /**
     * 取会话
     *
     * @param create 不存在时是否创建
     * @return 会话；{@code create} 为 false 且无会话时返回 null
     */
    ICHttpSession getSession(boolean create);

    /**
     * 取请求携带的 Cookie 列表
     *
     * @return Cookie 列表；未携带任何 Cookie 时返回空列表
     */
    List<ICCookie> getCookies();

    /**
     * 取请求体的二进制输入流（底层 Servlet 流的 JDK 父类型）
     *
     * @return 输入流
     * @throws IOException 取流失败，或已被 {@link #getReader} 占用时
     */
    InputStream getInputStream() throws IOException;

    /**
     * 以容器表单认证方式登录
     *
     * @param username 用户名
     * @param password 密码
     * @throws CServletException 认证失败时；底层 Servlet 异常经 cause 保留
     */
    void login(String username, String password);

    /**
     * 注销当前认证
     *
     * @throws CServletException 注销失败时；底层 Servlet 异常经 cause 保留
     */
    void logout();

}
