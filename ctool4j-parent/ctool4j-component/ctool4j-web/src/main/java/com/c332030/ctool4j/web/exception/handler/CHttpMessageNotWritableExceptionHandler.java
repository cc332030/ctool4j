package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.exception.annotation.ConditionalOnMissingExceptionHandler;
import lombok.CustomLog;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * Description: CHttpMessageNotWritableExceptionHandler
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>通过 {@code @ConditionalOnMissingExceptionHandler(HttpMessageNotWritableException.class)} 控制：容器存在其他同类型处理器时本处理器不生效。</li>
 *   <li>返回 void（直接结束响应，不写响应体）。</li>
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
 *     <td>异常对象为 null</td>
 *     <td>记录日志并直接结束响应</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>Web 应用统一响应消息不可写异常的响应格式。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>依赖 {@code CStrResult} 响应结构，与项目统一响应格式耦合。</li>
 * </ul>
 *
 * @since 2026/4/9
 * @version 1.0
 */
@CustomLog
@RestControllerAdvice
@ConditionalOnMissingExceptionHandler(HttpMessageNotWritableException.class)
public class CHttpMessageNotWritableExceptionHandler {

    /**
     * 处理响应消息不可写异常，仅记录日志
     *
     * @param e 响应消息不可写异常
     */
    @ExceptionHandler(HttpMessageNotWritableException.class)
    public void handle(HttpMessageNotWritableException e) {
        log.debug("handle HttpMessageNotWritableException，requestURI: {}", CRequestUtils.getRequestURIDefaultNull(), e);
    }

}
