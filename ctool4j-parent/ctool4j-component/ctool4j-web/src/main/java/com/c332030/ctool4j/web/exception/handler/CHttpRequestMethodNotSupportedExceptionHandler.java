package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.exception.annotation.ConditionalOnMissingExceptionHandler;
import lombok.CustomLog;
import lombok.val;
import org.springframework.core.annotation.Order;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * Description: CHttpRequestMethodNotSupportedExceptionHandler
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>通过 {@code @ConditionalOnMissingExceptionHandler(HttpRequestMethodNotSupportedException.class)} 控制：容器存在其他同类型处理器时本处理器不生效。</li>
 *   <li>返回 {@code CStrResult&lt;Void&gt;} 错误结果（{@code CStrResult.error(...)}）。</li>
 *   <li>记录请求 URI 与异常堆栈（log），保证问题可追溯。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <ul>
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
 *     <td>异常对象为 null（非预期，兜底不依赖异常内容）</td>
 *     <td>不取异常内容，返回异常消息（消息为 null 时用异常类型名）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>Web 应用统一请求方法不支持异常的响应格式。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>依赖 {@code CStrResult} 响应结构，与项目统一响应格式耦合。</li>
 * </ul>
 *
 * @since 2026/4/9
 * @version 1.2
 */
@CustomLog
@Order(CExceptionHandlerOrder.CONCRETE)
@RestControllerAdvice
@ConditionalOnMissingExceptionHandler(HttpRequestMethodNotSupportedException.class)
public class CHttpRequestMethodNotSupportedExceptionHandler {

    /**
     * 处理请求方法不支持异常
     * <p>{@code e} 为 null 属非预期入参（Spring MVC 命中 {@code @ExceptionHandler} 时异常对象不为 null）；
     * 异常消息为 null 时用异常类型简单名兜底，避免 {@code message: null}；见类 javadoc「兜底设计」。</p>
     *
     * @param e 请求方法不支持异常
     * @return 错误结果
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public CStrResult<Void> handle(HttpRequestMethodNotSupportedException e) {

        log.debug("handle HttpRequestMethodNotSupportedException，requestURI: {}", CRequestUtils.getRequestURIDefaultNull(), e);

        val message = null == e ? null : e.getMessage();
        if (null == message) {
            return CStrResult.error("请求方法不支持");
        }

        return CStrResult.error(message);
    }

}
