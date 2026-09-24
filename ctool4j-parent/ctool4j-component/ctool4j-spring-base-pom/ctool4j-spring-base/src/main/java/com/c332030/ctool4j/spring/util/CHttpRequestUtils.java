package com.c332030.ctool4j.spring.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CUrlUtils;
import com.c332030.ctool4j.definition.function.CBiConsumer;
import com.c332030.ctool4j.definition.function.StringFunction;
import com.c332030.ctool4j.interfaces.CHttpRequest;
import com.c332030.ctool4j.interfaces.CHttpResponse;
import com.google.common.net.HttpHeaders;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * <p>
 * Description: CHttpRequestUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CHttpRequestUtils}（{@code @UtilityClass}）是请求工具的<b>容器无关部分</b>：读取当前请求上下文
 * （Spring 的 {@code RequestContextHolder}），并对<b>给定的</b>抽象层请求/响应提供取用与加工。</p>
 * <ul>
 *   <li>{@code getServletRequestAttributes} / {@code hasRequest} / {@code noRequest}：当前请求上下文</li>
 *   <li>{@code getContextPathDefaultNull(request)} / {@code getRequestURIDefaultNull(request)}：请求路径</li>
 *   <li>{@code getHeader(request, name)} / {@code getHeaders(request, name)}：报文头取值</li>
 *   <li>{@code getHeaderThenDo(...)} / {@code getHeadersThenDo(...)}：报文头取值后执行动作（跳过空取值）</li>
 *   <li>{@code getReferer(request)} / {@code getRefererPath(request)} / {@code getRefererPathThenConvert(...)} / {@code getRefererPathThenConvertDefaultNull(...)}：来源页与来源页路径</li>
 *   <li>{@code getIp(request)}：客户端 IP（规则单一来源见 {@link CHttpRequest#getClientIp()}）</li>
 *   <li>{@code getAttrStr(request, name)}：请求属性转字符串</li>
 *   <li>{@code addPrepare} / {@code prepare} / {@code addClear} / {@code clear}：请求前初始化与请求结束清理回调</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>为什么单独一个公共类</b>：{@code CRequestUtils} 必须落在两侧适配模块（取当前请求要
 *   {@code CHttpServletRequest#of} / {@code CHttpServletResponse#of} 包装容器对象），但只有「取当前请求/响应」
 *   这一步需要容器，其余全部只用抽象层类型——无容器依赖的部分一律收敛到本类，
 *   使两侧 <b>同名类只留需要拆分的部分</b>、公共算法只存一份（见 {@code agent/AGENTS-PROJECT.MD}「javax/jakarta 双栈」）。</li>
 *   <li><b>两侧同名类调用本类，不继承</b>：两侧 {@code CRequestUtils} 的「当前请求」便捷入口把「取当前请求 + 本类的中立方法」
 *   组合起来；工具类之间不建立继承关系（同一份公共实现只通过调用复用）。</li>
 *   <li><b>入参为抽象层类型</b>：只接受 {@link CHttpRequest} / {@link CHttpResponse}，不接受任何 Servlet 包类型
 *   （对外签名不对容器表态）。</li>
 *   <li><b>入参为 null 的处置按方法语义</b>：{@code DefaultNull} 系列（{@link #getContextPathDefaultNull}、
 *   {@link #getRequestURIDefaultNull}、{@link #getRefererPathThenConvertDefaultNull}）对 null 请求返回 null；
 *   {@link #getHeaderThenDo} / {@link #getHeadersThenDo} 对 null 请求直接不执行；
 *   其余方法按入参直接使用（请求为 null 时抛 {@code NullPointerException}，与既有调用口径一致）。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>非请求上下文（{@link #getServletRequestAttributes()} 为 null）</td>
 *     <td>{@code hasRequest} 返回 false；其余方法各自按入参处理，本类不抛异常</td>
 *   </tr>
 *   <tr>
 *     <td>传入的请求为 null</td>
 *     <td>{@code DefaultNull} 系列返回 null；{@code getHeaderThenDo} / {@code getHeadersThenDo} 不执行动作；
 *     其余方法抛 {@code NullPointerException}（不静默兜底）</td>
 *   </tr>
 *   <tr>
 *     <td>回调集合为空</td>
 *     <td>{@code prepare} / {@code clear} 不执行任何动作</td>
 *   </tr>
 *   <tr>
 *     <td>回调执行抛异常</td>
 *     <td>捕获 {@code Throwable} 并记 error 日志，不中断其余回调</td>
 *   </tr>
 *   <tr>
 *     <td>{@code addPrepare} / {@code addClear} 传入 null</td>
 *     <td>抛 {@code NullPointerException}（不静默忽略空回调）</td>
 *   </tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>「对给定请求取用」的全部场景（调用方已有抽象层请求对象时直接用本类）；两侧同名 {@code CRequestUtils} 的公共实现来源。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>取<b>当前</b>请求/响应不适用（需要容器包装，见两侧同名类的 {@code getRequest} / {@code getResponse}）。</li>
 *   <li>需要容器专有能力（会话、Cookie、二进制流）时不适用：本层不认识容器类型，须由调用侧取底层对象
 *   （逃生口见 {@link #getServletRequestAttributes()}）。</li>
 *   <li>两侧<b>取值不同</b>的常量（如 {@code RequestDispatcher.ERROR_STATUS_CODE}）不在此声明的语义内，落在两侧同名类上。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>{@code prepare} / {@code clear} 回调为<b>静态</b>注册、跨请求存活（进程级），注册方须自行保证幂等与清理。</li>
 *   <li>{@code getIp} 的 {@code X-Forwarded-For} 信任口径见 {@link CHttpRequest#getClientIp()}（无条件信任首段，对外须由可信代理清理）。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.0
 */
@CustomLog
@UtilityClass
public class CHttpRequestUtils {

    /**
     * 请求前-初始化回调
     */
    private static final Set<CBiConsumer<CHttpRequest, CHttpResponse>> PREPARE_CONSUMERS = new CopyOnWriteArraySet<>();

    /**
     * 请求后-清洁工作回调
     */
    private static final Set<CBiConsumer<CHttpRequest, CHttpResponse>> CLEAR_CONSUMERS = new CopyOnWriteArraySet<>();

    /**
     * 注册请求前初始化回调
     *
     * @param consumer 请求前初始化回调，不能为 null
     */
    public void addPrepare(CBiConsumer<CHttpRequest, CHttpResponse> consumer) {
        PREPARE_CONSUMERS.add(Objects.requireNonNull(consumer));
    }

    /**
     * 执行全部请求前初始化回调；单个回调抛异常只记日志、不影响其余回调
     *
     * @param request  请求
     * @param response 响应
     */
    public void prepare(CHttpRequest request, CHttpResponse response) {
        PREPARE_CONSUMERS.forEach(consumer -> {
            try {
                consumer.accept(request, response);
            } catch (Throwable t) {
                log.error("prepare failure", t);
            }
        });
    }

    /**
     * 注册请求结束清理回调
     *
     * @param consumer 请求结束清理回调，不能为 null
     */
    public void addClear(CBiConsumer<CHttpRequest, CHttpResponse> consumer) {
        CLEAR_CONSUMERS.add(Objects.requireNonNull(consumer));
    }

    /**
     * 执行全部请求结束清理回调；单个回调抛异常只记日志、不影响其余回调
     *
     * @param request  请求
     * @param response 响应
     */
    public void clear(CHttpRequest request, CHttpResponse response) {
        CLEAR_CONSUMERS.forEach(consumer -> {
            try {
                consumer.accept(request, response);
            } catch (Throwable t) {
                log.error("clear failure", t);
            }
        });
    }

    /**
     * 取当前请求的属性（Spring 的请求上下文）
     *
     * <p>返回 Spring 自己的 {@code ServletRequestAttributes}，其 {@code getRequest()}/{@code getResponse()}
     * 都是某一容器包（javax 或 jakarta）的类型。本方法是本工具族的<b>唯一容器逃生口</b>，只供抽象层表达不了的
     * 容器专有能力（二进制输出流、会话、Cookie）使用；其余场景一律走抽象层入口。</p>
     *
     * @return 当前请求的属性；非请求上下文时返回 null
     */
    public ServletRequestAttributes getServletRequestAttributes() {
        return (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    }

    /**
     * 是否是接口请求
     *
     * @return 结果
     */
    public boolean hasRequest() {
        return null != getServletRequestAttributes();
    }

    /**
     * @return 结果
     * @see CHttpRequestUtils#hasRequest()
     */
    public boolean noRequest() {
        return !hasRequest();
    }

    /**
     * 取 Context Path，可为空
     *
     * @param request 请求；为 null 时返回 null
     * @return Context Path
     */
    public String getContextPathDefaultNull(CHttpRequest request) {
        return CObjUtils.convert(request, CHttpRequest::getContextPath);
    }

    /**
     * 取 RequestURI，可为空
     *
     * @param request 请求；为 null 时返回 null
     * @return RequestURI
     */
    public String getRequestURIDefaultNull(CHttpRequest request) {
        return CObjUtils.convert(request, CHttpRequest::getRequestURI);
    }

    /**
     * 获取 Header
     *
     * @param request 请求
     * @param header  Header Name
     * @return Header Value
     */
    public String getHeader(CHttpRequest request, String header) {
        return request.getHeader(header);
    }

    /**
     * 获取 Headers
     *
     * @param request 请求
     * @param header  Header Name
     * @return Header Values
     */
    public List<String> getHeaders(CHttpRequest request, String header) {
        val valueEnumeration = request.getHeaders(header);
        return CCollUtils.getValues(valueEnumeration);
    }

    /**
     * 获取 Header，并做动作；取不到值（null 或空串）的头不执行动作，请求为空时全部不执行
     *
     * @param request     请求
     * @param headerNames Header Names
     * @param biConsumer  动作
     */
    public void getHeaderThenDo(CHttpRequest request, Collection<String> headerNames, CBiConsumer<String, String> biConsumer) {

        if (null == request || CollUtil.isEmpty(headerNames)) {
            return;
        }

        headerNames.forEach(headerName -> {

            val headerValue = getHeader(request, headerName);
            if (StrUtil.isEmpty(headerValue)) {
                return;
            }
            biConsumer.accept(headerName, headerValue);
        });

    }

    /**
     * 获取 Headers，并做动作；取不到值（null 或空集合）的头不执行动作，请求为空时全部不执行
     *
     * @param request     请求
     * @param headerNames Header Names
     * @param biConsumer  动作
     */
    public void getHeadersThenDo(CHttpRequest request, Collection<String> headerNames, CBiConsumer<String, List<String>> biConsumer) {

        if (null == request || CollUtil.isEmpty(headerNames)) {
            return;
        }

        headerNames.forEach(headerName -> {

            val headerValues = getHeaders(request, headerName);
            if (CollUtil.isEmpty(headerValues)) {
                return;
            }
            biConsumer.accept(headerName, headerValues);
        });

    }

    /**
     * 获取 Referer
     *
     * @param request 请求
     * @return Referer；无该头时返回 null
     */
    public String getReferer(CHttpRequest request) {
        return request.getHeader(HttpHeaders.REFERER);
    }

    /**
     * 获取 Referer Path
     *
     * @param request 请求
     * @return Referer Path
     */
    public String getRefererPath(CHttpRequest request) {
        return CUrlUtils.getPath(getReferer(request));
    }

    /**
     * 获取 Referer Path，并做转换
     *
     * @param request  请求
     * @param function 转换方法
     * @param <T>      目标泛型
     * @return 目标；路径为空时返回 null
     */
    public <T> T getRefererPathThenConvert(CHttpRequest request, StringFunction<T> function) {
        val path = getRefererPath(request);
        if (StrUtil.isEmpty(path)) {
            return null;
        }
        return function.apply(path);
    }

    /**
     * 获取 Referer Path，并做转换，异常时返回 null
     *
     * @param request  请求
     * @param function 转换方法
     * @param <T>      目标泛型
     * @return 目标；路径为空或转换失败时返回 null
     */
    public <T> T getRefererPathThenConvertDefaultNull(CHttpRequest request, StringFunction<T> function) {
        try {
            return getRefererPathThenConvert(request, function);
        } catch (Exception e) {
            log.debug("转换 Referer 失败", e);
            return null;
        }
    }

    /**
     * 获取 Ip
     * <p>注意：当前实现无条件信任 X-Forwarded-For 首段，客户端直连时
     * 可伪造该请求头绕过 IP 校验/风控，属安全问题。
     * 修复方案：仅信任来自已配置可信代理的 X-Forwarded-For（默认不信任，
     * 未配置时忽略该头直接返回 remoteAddr）；当前业务场景较小，暂未修复</p>
     *
     * @param request 请求
     * @return Ip
     */
    public String getIp(CHttpRequest request) {
        // 单一来源：IP 解析规则收在抽象层的 CHttpRequest#getClientIp
        return request.getClientIp();
    }

    /**
     * 获取请求属性并转为字符串（null 属性返回 null），可为空
     *
     * @param request       请求
     * @param attributeName 属性名
     * @return 属性字符串
     */
    public String getAttrStr(CHttpRequest request, String attributeName) {
        return StrUtil.toStringOrNull(request.getAttribute(attributeName));
    }

}
