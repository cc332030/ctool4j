package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.core.exception.CUnauthorizedException;
import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.exception.annotation.ConditionalOnMissingExceptionHandler;
import lombok.CustomLog;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * Description: CUnauthorizedExceptionHandler：未授权异常统一返回业务码 401
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>通过 {@code @ConditionalOnMissingExceptionHandler(CUnauthorizedException.class)} 控制：容器存在其他同类型处理器时本处理器不生效。</li>
 *   <li>返回 {@code CStrResult&lt;Void&gt;} 错误结果：{@code code} = "401"，{@code message} 取异常消息（为空回退 401 状态原因短语）。</li>
 *   <li>响应体的业务码固定为 401；异常消息为空时 message 回退 401 状态原因短语。</li>
 *   <li>记录请求 URI 与异常堆栈（log），保证问题可追溯。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>业务码取 401（与 {@code HttpStatus.UNAUTHORIZED} 同值——本项目状态码常量即取 HTTP 状态码语义），与认证链路（{@code CAuthenticationEntryPoint}、
 *   {@code CSessionInformationExpiredStrategy} 经 {@code CSpringSecurityUtils#writeJsonError} 输出）口径一致：
 *   同一次「未认证」无论由过滤器拦截还是由业务代码取当前会话失败触发，响应形状一致。</li>
 *   <li>本处理器是 ctool4j-web 中<b>唯一</b>同时设置响应体业务码与 HTTP 响应状态的异常处理器：未授权的对外契约是
 *   <b>响应体业务码 401</b>（与模块内其余处理器同一表意口径——HTTP 200 + 响应体业务码），随附 HTTP 状态 401 仅为
 *   让通用 HTTP 客户端、网关与监控在不解析响应体时也能识别；调用方应以响应体业务码为准。</li>
 *   <li>用 {@code CRequestUtils.getRequestURIDefaultNull()} 记录请求 URI 用于日志。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>容器已存在同类型处理器</td>
 *     <td>@ConditionalOnMissingExceptionHandler 使本处理器不生效</td>
 *   </tr>
 *   <tr>
 *     <td>异常消息为 null/空</td>
 *     <td>code = "401"，message 回退 401 状态原因短语</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要「未认证」响应体业务码 401 语义的 Web 应用：当前会话缺失/失效、凭据无效等。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>已认证但无权限属 403（Forbidden），不应抛 {@code CUnauthorizedException}。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>业务码 401 与模块内其他处理器的业务码体系一致；HTTP 响应状态 401 为随附信息（其余处理器为 HTTP 200），
 *   调用方若以 HTTP 状态判断成功与否，会与以响应体业务码判断的结果不一致。</li>
 *   <li>依赖 {@code CStrResult} 响应结构，与项目统一响应格式耦合。</li>
 *   <li>HTTP 状态 401 未设置 {@code WWW-Authenticate} 响应头（HTTP 层 401 的建议项），如需可另行补充。</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/17
 * @version 1.1
 * <p>用例见 {@code CUnauthorizedExceptionHandlerTests}（主代码类注释不 {@code @see} 测试类：javadoc 类路径不含测试源）。</p>
 * @see "doc/design/web/unauthorized-401.adoc"
 */
@CustomLog
@RestControllerAdvice
@ConditionalOnMissingExceptionHandler(CUnauthorizedException.class)
public class CUnauthorizedExceptionHandler {

    /**
     * 处理未授权异常
     *
     * @param e 未授权异常
     * @return 错误结果（业务码 "401"）
     */
    @ExceptionHandler(CUnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public CStrResult<Void> handle(CUnauthorizedException e) {

        log.debug("handle CUnauthorizedException，requestURI: {}", CRequestUtils.getRequestURIDefaultNull(), e);

        return CStrResult.error(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

}
