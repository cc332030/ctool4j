package com.c332030.ctool4j.spring.security.core;

import com.c332030.ctool4j.model.CHttpServletRequest;
import com.c332030.ctool4j.model.CHttpServletResponse;
import com.c332030.ctool4j.spring.security.interfaces.CForbiddenHandler;
import lombok.CustomLog;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Description: CAccessDeniedHandler
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CAccessDeniedHandler} 是访问拒绝处理的<b>javax 侧适配器</b>：实现 Spring Security 的
 * {@link AccessDeniedHandler}（其签名固定为 javax Servlet 类型），把请求/响应包装成抽象层对象后，
 * 委托 {@link CForbiddenHandler} 处理。</p>
 * <ul>
 *   <li>{@code handle(request, response, accessDeniedException)}：打 debug 日志 → 包装 → 委托</li>
 *   <li>{@code CAccessDeniedHandler(forbiddenHandler)}：注入自定义处理器，替换默认写出策略</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>只做适配，不含业务</b>：403 的写出在抽象层的 {@code CDefaultForbiddenHandler} 里，
 *   本类不做第二份实现——避免 javax/jakarta 两侧逻辑漂移。</li>
 *   <li><b>同名类成对存在</b>：jakarta 侧有同包同名的 {@code CAccessDeniedHandler}，
 *   使用方改依赖模块即切换容器，类名与用法不变。</li>
 *   <li><b>可替换的处理器</b>：默认构造用 {@code CDefaultForbiddenHandler}；
 *   需要自定义响应形状时用带参构造注入自己的 {@code CForbiddenHandler}。</li>
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
 *     <td>任何拒绝原因</td>
 *     <td>统一 403 + 标准文案，不回传被拒绝的资源或权限信息</td>
 *   </tr>
 *   <tr>
 *     <td>写出失败</td>
 *     <td>抛 {@code CServletException}，不静默吞掉</td>
 *   </tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>javax（Spring Boot 2.x / Servlet 4.0）工程中的访问拒绝统一处理，由 {@code CSecurityConfiguration} 自动装配。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>jakarta（Servlet 5.0+）工程须改用 jakarta 侧的同名类，否则方法签名不匹配。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>拒绝原因只进日志、不进响应体：避免把权限模型细节暴露给调用方。</li>
 * </ul>
 *
 * @since 2026/1/28
 * @version 1.1
 */
@CustomLog
public class CAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * 抽象层访问拒绝处理器（默认实现；可经带参构造替换）
     */
    private final CForbiddenHandler forbiddenHandler;

    /**
     * 使用默认访问拒绝处理器构造
     */
    public CAccessDeniedHandler() {
        this(new CDefaultForbiddenHandler());
    }

    /**
     * 使用指定访问拒绝处理器构造
     *
     * @param forbiddenHandler 访问拒绝处理器，不能为 null
     */
    public CAccessDeniedHandler(CForbiddenHandler forbiddenHandler) {
        this.forbiddenHandler = forbiddenHandler;
    }

    /**
     * 处理访问被拒绝：包装为抽象层对象后委托处理器输出 403
     *
     * @param request               请求
     * @param response              响应
     * @param accessDeniedException 访问拒绝异常
     */
    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException
    ) {

        log.debug("CAccessDeniedHandler", accessDeniedException);

        forbiddenHandler.handle(
            CHttpServletRequest.of(request),
            CHttpServletResponse.of(response),
            accessDeniedException
        );

    }

}
