package com.c332030.ctool4j.spring.util;

import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.core.util.COpt;
import com.c332030.ctool4j.definition.function.CBiConsumer;
import com.c332030.ctool4j.definition.function.StringFunction;
import com.c332030.ctool4j.interfaces.CHttpRequest;
import com.c332030.ctool4j.interfaces.CHttpResponse;
import com.c332030.ctool4j.model.CHttpServletRequest;
import com.c332030.ctool4j.model.CHttpServletResponse;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.RequestDispatcher;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * Description: CRequestUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CRequestUtils} 是请求工具在 <b>jakarta</b> 侧的落地：<b>只保留必须接触容器的方法</b>——
 * 取当前请求/响应（要经 {@link CHttpServletRequest#of} / {@link CHttpServletResponse#of} 包装容器对象），
 * 以及在<b>当前请求</b>上取值的便捷入口（取当前请求 + {@link CHttpRequestUtils} 中立方法的组合）。</p>
 * <ul>
 *   <li>{@code getRequestDefaultNull} / {@code getRequestOpt} / {@code getRequest}：当前请求（抽象层）</li>
 *   <li>{@code getResponseDefaultNull} / {@code getResponseOpt} / {@code getResponse}：当前响应（抽象层）</li>
 *   <li>{@code getContextPathDefaultNull} / {@code getContextPath} / {@code getRequestURIDefaultNull} / {@code getRequestURI}：当前请求的路径</li>
 *   <li>{@code getHeader} / {@code getHeaders} / {@code getHeaderThenDo} / {@code getHeadersThenDo}：当前请求的报文头</li>
 *   <li>{@code getReferer} / {@code getRefererPath} / {@code getRefererPathThenConvert} / {@code getRefererPathThenConvertDefaultNull}：当前请求的来源页</li>
 *   <li>{@code getIp}：当前请求的客户端 IP</li>
 *   <li>{@code getErrorStatusCode}：错误状态码（常量两侧取值不同，见「设计要点」）</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>容器无关的部分在公共类</b>：{@link CHttpRequestUtils} 承载「对给定请求取用」的全部方法与请求上下文读取；
 *   本类<b>只调用它、不继承它</b>（工具类之间不建立继承关系），故本类只留必须接触容器的方法，公共算法不复制。</li>
 *   <li><b>只有包装与便捷入口需要容器</b>：取当前请求要拿 Spring 的 {@code ServletRequestAttributes} 里的容器请求再包装；
 *   无参便捷入口（如 {@code getHeader(name)}）本质是「取当前请求 + 公共类中立方法」的一行组合。</li>
 *   <li><b>对外只暴露抽象层类型</b>：方法返回 {@link CHttpRequest} / {@link CHttpResponse}，使用方据此编程即不绑定 Servlet 包。</li>
 *   <li><b>容器逃生口在公共类</b>：{@link CHttpRequestUtils#getServletRequestAttributes()} 返回 Spring 的
 *   {@code ServletRequestAttributes}，只供抽象层表达不了的容器专有能力（二进制输出流、会话、Cookie）使用。</li>
 *   <li><b>两侧取值不同的常量落本类</b>：{@link #getErrorStatusCode} 取的
 *   {@code RequestDispatcher.ERROR_STATUS_CODE} 在两侧分别是 {@code jakarta.servlet.error.status_code} 与
 *   {@code jakarta.servlet.error.status_code}，只能落在两侧同名类上（切模块即切值）。</li>
 *   <li><b>同名同构</b>：本类在两侧适配模块中同包同名（本份承接 jakarta 侧），使用方切换依赖模块即可切换容器。</li>
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
 *     <td>非请求上下文</td>
 *     <td>{@code getXxxDefaultNull} 返回 null；{@code getXxx} 抛 {@link IllegalArgumentException}；便捷入口按同样口径</td>
 *   </tr>
 *   <tr>
 *     <td>{@code getRefererPathThenConvertDefaultNull} 转换失败</td>
 *     <td>记 debug 日志并返回 null（不向调用方抛异常）</td>
 *   </tr>
 *   <tr>
 *     <td>错误状态码属性不存在</td>
 *     <td>{@link #getErrorStatusCode} 返回 null（由调用方决定默认状态码）</td>
 *   </tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <p>请求处理线程内取用当前请求/响应；需要在两套 Servlet 容器间可切换的公共代码。</p>
 *
 * <h2>不适用与边界场景</h2>
 * <p>非请求线程（定时任务、异步线程）取不到上下文；需要容器专有能力时经
 * {@link CHttpRequestUtils#getServletRequestAttributes()} 取底层对象（容器类型只在调用处出现一次）。</p>
 *
 * <h2>已知限制与取舍</h2>
 * <p>本类只做「取当前请求/响应 + 包装 + 一行组合」，全部加工逻辑在 {@link CHttpRequestUtils}；
 * 取用当前请求每次都新建一个包装对象，成本为一次对象创建。</p>
 *
 * @since 2024/12/9
 * @version 1.2
 */
@CustomLog
@UtilityClass
public class CRequestUtils {

    /**
     * 获取请求（抽象层），可为空
     *
     * @return 抽象层请求；非请求上下文时返回 null
     */
    public CHttpRequest getRequestDefaultNull() {
        val request = CObjUtils.convert(CHttpRequestUtils.getServletRequestAttributes(), ServletRequestAttributes::getRequest);
        if(null == request) {
            return null;
        }
        return CHttpServletRequest.of(request);
    }

    /**
     * 获取请求（抽象层）的 COpt
     *
     * @return COpt 抽象层请求
     */
    public COpt<CHttpRequest> getRequestOpt() {
        return COpt.ofNullable(getRequestDefaultNull());
    }

    /**
     * 获取请求（抽象层），不能为空
     *
     * @return 抽象层请求，不能为空
     */
    public CHttpRequest getRequest() {
        return getRequestOpt()
                .orElseThrow(() -> new IllegalArgumentException("request 不能为空"));
    }

    /**
     * 获取响应（抽象层），可为空
     *
     * @return 抽象层响应；非请求上下文或容器未创建响应时返回 null
     */
    public CHttpResponse getResponseDefaultNull() {
        val response = CObjUtils.convert(CHttpRequestUtils.getServletRequestAttributes(), ServletRequestAttributes::getResponse);
        if(null == response) {
            return null;
        }
        return CHttpServletResponse.of(response);
    }

    /**
     * 获取响应（抽象层）的 COpt
     *
     * @return COpt 抽象层响应
     */
    public COpt<CHttpResponse> getResponseOpt() {
        return COpt.ofNullable(getResponseDefaultNull());
    }

    /**
     * 获取响应（抽象层），不能为空
     *
     * @return 抽象层响应，不能为空
     */
    public CHttpResponse getResponse() {
        return getResponseOpt()
                .orElseThrow(() -> new IllegalArgumentException("response 不能为空"));
    }

    /**
     * 获取 Context Path，可为空
     *
     * @return Context Path
     */
    public String getContextPathDefaultNull() {
        return CHttpRequestUtils.getContextPathDefaultNull(getRequestDefaultNull());
    }

    /**
     * 获取 Context Path，不能为空
     *
     * @return Context Path
     */
    public String getContextPath() {
        return Optional.ofNullable(getContextPathDefaultNull())
                .orElseThrow(() -> new IllegalArgumentException("contextPath 不能为空"));
    }

    /**
     * 获取 RequestURI，可为空
     *
     * @return RequestURI
     */
    public String getRequestURIDefaultNull() {
        return CHttpRequestUtils.getRequestURIDefaultNull(getRequestDefaultNull());
    }

    /**
     * 获取 RequestURI，不能为空
     *
     * @return RequestURI
     */
    public String getRequestURI() {
        return Optional.ofNullable(getRequestURIDefaultNull())
                .orElseThrow(() -> new IllegalArgumentException("requestURI 不能为空"));
    }

    /**
     * 获取 Header
     *
     * @param header Header Name
     * @return Header Value
     */
    public String getHeader(String header) {
        return CHttpRequestUtils.getHeader(getRequest(), header);
    }

    /**
     * 获取 Headers
     *
     * @param header Header Name
     * @return Header Values
     */
    public List<String> getHeaders(String header) {
        return CHttpRequestUtils.getHeaders(getRequest(), header);
    }

    /**
     * 获取 Header，并做动作
     *
     * @param headerNames Header Names
     * @param biConsumer  动作
     */
    public void getHeaderThenDo(Collection<String> headerNames, CBiConsumer<String, String> biConsumer) {
        CHttpRequestUtils.getHeaderThenDo(getRequestDefaultNull(), headerNames, biConsumer);
    }

    /**
     * 获取 Headers，并做动作
     *
     * @param headerNames Header Names
     * @param biConsumer  动作
     */
    public void getHeadersThenDo(Collection<String> headerNames, CBiConsumer<String, List<String>> biConsumer) {
        CHttpRequestUtils.getHeadersThenDo(getRequestDefaultNull(), headerNames, biConsumer);
    }

    /**
     * 获取 Referer
     *
     * @return Referer
     */
    public String getReferer() {
        return CHttpRequestUtils.getReferer(getRequest());
    }

    /**
     * 获取 Referer Path
     *
     * @return Referer Path
     */
    public String getRefererPath() {
        return CHttpRequestUtils.getRefererPath(getRequest());
    }

    /**
     * 获取 Referer Path，并做转换
     *
     * @param function 转换方法
     * @param <T>      目标泛型
     * @return 目标
     */
    public <T> T getRefererPathThenConvert(StringFunction<T> function) {
        return CHttpRequestUtils.getRefererPathThenConvert(getRequest(), function);
    }

    /**
     * 获取 Referer Path，并做转换，默认为空
     *
     * @param function 转换方法
     * @param <T>      目标泛型
     * @return 目标；非请求上下文或转换失败时返回 null
     */
    public <T> T getRefererPathThenConvertDefaultNull(StringFunction<T> function) {
        // 非请求环境由本层的取当前请求入口判定（抛异常），故守卫留在本层；转换逻辑不重复实现
        try {
            return CHttpRequestUtils.getRefererPathThenConvert(getRequest(), function);
        } catch (Exception e) {
            log.debug("转换 Referer 失败", e);
            return null;
        }
    }

    /**
     * 获取 Ip
     *
     * @return Ip
     */
    public String getIp() {
        return CHttpRequestUtils.getIp(getRequest());
    }

    /**
     * 获取错误状态码（取自 RequestDispatcher.ERROR_STATUS_CODE 属性），可为空
     *
     * @param request 抽象层请求
     * @return 错误状态码字符串
     */
    public String getErrorStatusCode(CHttpRequest request) {
        return CHttpRequestUtils.getAttrStr(request, RequestDispatcher.ERROR_STATUS_CODE);
    }

}
