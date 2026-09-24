package com.c332030.ctool4j.spring.security.core;

import com.c332030.ctool4j.model.CHttpServletRequest;
import com.c332030.ctool4j.model.CHttpServletResponse;
import com.c332030.ctool4j.spring.security.interfaces.CUnauthorizedHandler;
import lombok.CustomLog;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * <p>
 * Description: CAuthenticationEntryPoint
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CAuthenticationEntryPoint} 是未认证处理的<b>jakarta 侧适配器</b>：实现 Spring Security 的
 * {@link AuthenticationEntryPoint}（其签名固定为 jakarta Servlet 类型），把请求/响应包装成抽象层对象后，
 * 委托 {@link CUnauthorizedHandler} 处理。</p>
 * <ul>
 *   <li>{@code commence(request, response, authenticationException)}：打 debug 日志 → 包装 → 委托</li>
 *   <li>{@code CAuthenticationEntryPoint(unauthorizedHandler)}：注入自定义处理器，替换默认文案/写出策略</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>只做适配，不含业务</b>：文案判定与 401 写出都在抽象层的
 *   {@code CDefaultUnauthorizedHandler} 里，本类不做第二份实现——避免 jakarta/javax 两侧逻辑漂移。</li>
 *   <li><b>同名类成对存在</b>：javax 侧有同包同名的 {@code CAuthenticationEntryPoint}，
 *   使用方改依赖模块即切换容器，类名与用法不变。</li>
 *   <li><b>可替换的处理器</b>：默认构造用 {@code CDefaultUnauthorizedHandler}；
 *   需要自定义响应形状时用带参构造注入自己的 {@code CUnauthorizedHandler}。</li>
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
 *     <td>异常类型不在已知分支内</td>
 *     <td>由默认处理器回退为 401 的标准文案</td>
 *   </tr>
 *   <tr>
 *     <td>写出失败</td>
 *     <td>抛 {@code CServletException}，不静默吞掉</td>
 *   </tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>jakarta（Spring Boot 2.x / Servlet 4.0）工程中的未认证统一处理，由 {@code CSecurityConfiguration} 自动装配。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>javax（Servlet 5.0+）工程须改用 javax 侧的同名类，否则方法签名不匹配。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>只输出响应体，不设置 {@code WWW-Authenticate} 响应头（与项目既有口径一致）。</li>
 * </ul>
 *
 * @since 2026/1/26
 * @version 1.1
 */
@CustomLog
public class CAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * 抽象层未认证处理器（默认实现；可经带参构造替换）
     */
    private final CUnauthorizedHandler unauthorizedHandler;

    /**
     * 使用默认未认证处理器构造
     */
    public CAuthenticationEntryPoint() {
        this(new CDefaultUnauthorizedHandler());
    }

    /**
     * 使用指定未认证处理器构造
     *
     * @param unauthorizedHandler 未认证处理器，不能为 null
     */
    public CAuthenticationEntryPoint(CUnauthorizedHandler unauthorizedHandler) {
        this.unauthorizedHandler = unauthorizedHandler;
    }

    /**
     * 处理未认证访问：包装为抽象层对象后委托处理器输出 401
     *
     * @param request                请求
     * @param response               响应
     * @param authenticationException 认证异常
     */
    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authenticationException
    ) {

        log.debug("CAuthenticationEntryPoint", authenticationException);

        unauthorizedHandler.handle(
            CHttpServletRequest.of(request),
            CHttpServletResponse.of(response),
            authenticationException
        );

    }

}
