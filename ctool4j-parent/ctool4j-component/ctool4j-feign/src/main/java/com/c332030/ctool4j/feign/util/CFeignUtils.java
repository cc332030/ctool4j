package com.c332030.ctool4j.feign.util;

import cn.hutool.core.collection.CollUtil;
import com.c332030.ctool4j.core.util.CMapUtils;
import com.c332030.ctool4j.definition.function.CConsumer;
import com.c332030.ctool4j.feign.config.CFeignClientHeaderConfig;
import com.c332030.ctool4j.feign.enums.CFeignClientHeaderPropagationModeEnum;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import feign.RequestTemplate;
import feign.Response;
import lombok.CustomLog;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Description: CFeignUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CFeignUtils}（{@code @UtilityClass} + {@code @CAutowiredScan}）提供 Feign 工具：</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>headerConfig 为 null</td>
 *     <td>transferHeaders 无操作</td>
 *   </tr>
 *   <tr>
 *     <td>无拦截器注册</td>
 *     <td>intercept 返回 false</td>
 *   </tr>
 *   <tr>
 *     <td>模板无 feignTarget</td>
 *     <td>getApiType 抛 NPE</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>Feign 接口级拦截与请求头传播控制。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>拦截按接口类型精确匹配；header 透传依赖 {@code CRequestUtils}（Servlet 请求上下文）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>拦截器</b></p>
 * <ul>
 *   <li>{@code INTERCEPTOR_MAP}（ConcurrentHashMap）缓存接口类 → 拦截处理。</li>
 *   <li>{@code intercept} 命中接口类型且有注册处理时执行并返回 true。</li>
 * </ul>
 * <p><b>header 透传</b></p>
 * <ul>
 *   <li>按 {@code headerConfig.propagationMode}：ALL（全部复制）、CUSTOM（自定义 headers）、NONE（不透传）。</li>
 *   <li>基于 {@code CRequestUtils} 读取当前请求 header 并复制到模板。</li>
 * </ul>
 *
 * @since 2025/9/21
 * @version 1.0
 */
@CustomLog
@UtilityClass
@CAutowiredScan
public class CFeignUtils {

    /**
     * HTTP 日志线程局部变量
     */
    public final ThreadLocal<StringBuilder> HTTP_LOG_THREAD_LOCAL = ThreadLocal.withInitial(StringBuilder::new);

    private static final Map<Class<?>, CConsumer<RequestTemplate>> INTERCEPTOR_MAP = new ConcurrentHashMap<>();

    @Setter
    @CAutowired
    CFeignClientHeaderConfig headerConfig;

    /**
     * 注册指定类的请求拦截器
     * <ul>
     *   <li>{@code addInterceptor(clazz, consumer)}：按接口类注册请求拦截器（{@code INTERCEPTOR_MAP}）。</li>
     * </ul>
     *
     * @param clazz    类
     * @param consumer 拦截处理
     */
    public void addInterceptor(Class<?> clazz, CConsumer<RequestTemplate> consumer) {

        log.debug("addInterceptor to: {}, consumer: {}", clazz, consumer);
        INTERCEPTOR_MAP.put(clazz, consumer);

    }

    /**
     * 获取请求对应的 Feign 接口类型
     * <ul>
     *   <li>{@code getApiType(template)}：获取请求对应 Feign 接口类型。</li>
     * </ul>
     *
     * @param template 请求模板
     * @return Feign 接口类型
     */
    public Class<?> getApiType(RequestTemplate template) {
        return template.feignTarget().type();
    }

    /**
     * 对请求模板执行拦截（按接口类型匹配）
     * <ul>
     *   <li>{@code intercept(template)} / {@code intercept(type, template)}：判断是否命中拦截器。</li>
     * </ul>
     *
     * @param template 请求模板
     * @return 是否命中拦截器
     */
    public boolean intercept(RequestTemplate template) {
        val type = getApiType(template);
        return intercept(type, template);
    }

    /**
     * 对请求模板执行拦截
     *
     * @param type     接口类型
     * @param template 请求模板
     * @return 是否命中拦截器
     */
    public boolean intercept(Class<?> type, RequestTemplate template) {

        for (val entry : INTERCEPTOR_MAP.entrySet()) {
            if(entry.getKey().isAssignableFrom(type)) {
                entry.getValue().accept(template);
                return true;
            }
        }
        return false;
    }

    /**
     * 构建新的响应
     * <ul>
     *   <li>{@code newResponse(response, bodyBytes)}：重建响应。</li>
     * </ul>
     *
     * @param response 原始响应
     * @param responseBodyBytes 响应体字节数组
     * @return 新的响应
     */
    public Response newResponse(Response response, byte[] responseBodyBytes) {

        val request = response.request();

        return Response.builder()
            .requestTemplate(request.requestTemplate())
            // TODO 低版本不支持
            // .protocolVersion(response.protocolVersion())
            .status(response.status())
            .reason(response.reason())
            .request(request)
            .headers(response.headers())
            .body(responseBodyBytes)
            .build();
    }

    /**
     * 按配置将请求头转移到 Feign 请求模板
     * <ul>
     *   <li>{@code transferHeaders(template)}：按 headerConfig 透传请求头。</li>
     * </ul>
     *
     * @param template 请求模板
     */
    public void transferHeaders(RequestTemplate template) {

        if(null == headerConfig) {
            return;
        }

        val propagationMode = headerConfig.getPropagationMode();
        val propagationRequestHeaders = headerConfig.getPropagationRequestHeaders();

        if(propagationMode == CFeignClientHeaderPropagationModeEnum.ALL
            && CollUtil.isEmpty(propagationRequestHeaders)
        ) {
            log.debug("propagation all headers and no propagation request headers");
            return;
        }

        val propagationCustomHeaders = headerConfig.getPropagationCustomHeaders();

        val originHeaders = CMapUtils.defaultEmpty(template.headers());
        val newHeaders = new LinkedHashMap<String, Collection<String>>(
            propagationCustomHeaders.size() + propagationRequestHeaders.size()
        );

        switch (propagationMode) {
            case ALL:
                newHeaders.putAll(originHeaders);
                break;
            case CUSTOM:
                originHeaders.forEach((header, values) -> {
                    if(propagationCustomHeaders.contains(header)
                        && CollUtil.isNotEmpty(values)
                    ) {
                        newHeaders.put(header, values);
                    }
                });
                break;
            case NONE:
                break;
        }
        log.debug("propagationMode: {}, propagationCustomHeaders: {}, propagationRequestHeaders: {}",
            propagationMode, propagationCustomHeaders, propagationRequestHeaders);

        CRequestUtils.getHeadersThenDo(propagationRequestHeaders, newHeaders::put);

        log.debug("newHeaders: {}", newHeaders);
        template.headers(null);
        template.headers(newHeaders);

    }

}
