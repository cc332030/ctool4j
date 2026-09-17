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
 * Description: CUnauthorizedExceptionHandler：未授权异常统一返回 401
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>通过 {@code @ConditionalOnMissingExceptionHandler(CUnauthorizedException.class)} 控制：容器存在其他同类型处理器时本处理器不生效。</li>
 *   <li>返回 {@code CStrResult&lt;Void&gt;} 错误结果：{@code code} = "401"，{@code message} 取异常消息（为空回退 401 状态原因短语）。</li>
 *   <li>HTTP 状态置 401（{@code @ResponseStatus(HttpStatus.UNAUTHORIZED)}）。</li>
 *   <li>记录请求 URI 与异常堆栈（log），保证问题可追溯。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>业务码取 HTTP 状态码（"401"），与认证链路（{@code CAuthenticationEntryPoint}、
 *   {@code CSessionInformationExpiredStrategy} 经 {@code CSpringSecurityUtils#writeJsonError} 输出）口径一致：
 *   同一次「未认证」无论由过滤器拦截还是由业务代码取当前会话失败触发，响应形状一致。</li>
 *   <li>本处理器是 ctool4j-web 中<b>唯一</b>显式设置 HTTP 状态的异常处理器（其余一律 HTTP 200 + 业务码表意）：
 *   未授权在 HTTP 语义上就是 401（RFC 7235），需让通用 HTTP 客户端、网关与监控可直接识别，故刻意偏离。</li>
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
 *     <td>code = "401"，message 取 401 状态原因短语</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要「未认证」HTTP 401 语义的 Web 应用：当前会话缺失/失效、凭据无效等。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>已认证但无权限属 403（Forbidden），不应抛 {@code CUnauthorizedException}。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>HTTP 状态为 401，与模块内其他处理器（HTTP 200）不一致：调用方/前端若以「HTTP 200 即成功」判断，需相应调整。</li>
 *   <li>依赖 {@code CStrResult} 响应结构，与项目统一响应格式耦合。</li>
 *   <li>未设置 {@code WWW-Authenticate} 响应头（RFC 7235 建议项），如需可另行补充。</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/17
 * @version 1.0
 */
@CustomLog
@RestControllerAdvice
@ConditionalOnMissingExceptionHandler(CUnauthorizedException.class)
public class CUnauthorizedExceptionHandler {

    /**
     * 处理未授权异常
     *
     * @param e 未授权异常
     * @return 错误结果（HTTP 401，code "401"）
     */
    @ExceptionHandler(CUnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public CStrResult<Void> handle(CUnauthorizedException e) {

        log.debug("handle CUnauthorizedException，requestURI: {}", CRequestUtils.getRequestURIDefaultNull(), e);

        return CStrResult.error(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

}
