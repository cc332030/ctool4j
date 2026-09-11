package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.exception.annotation.ConditionalOnMissingExceptionHandler;
import lombok.CustomLog;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * Description: CThrowableHandler
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CThrowableHandler} 标注 {@code @RestControllerAdvice}，通过 {@code @ExceptionHandler(Throwable.class)} 处理未识别异常：</p>
 * <ul>
 *   <li>通过 {@code @ConditionalOnMissingExceptionHandler(Throwable.class)} 控制：容器存在其他同类型处理器时本处理器不生效。</li>
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
 *     <td>异常对象为 null</td>
 *     <td>记录日志并返回错误结果</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>Web 应用统一未识别异常的响应格式。</li>
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
@ConditionalOnMissingExceptionHandler(Throwable.class)
public class CThrowableHandler {

    /**
     * 兜底处理未识别异常
     * <p>有意设计：捕获并完整记录异常（log.error 带堆栈），保证问题可追溯、异常不穿透到容器默认错误页；
     * 统一返回 200 业务 JSON 保持响应结构一致。已知边界：HTTP 语义缺失，无法按 5xx 触发告警
     * （含 OOM、StackOverflow 等 Error 也返回 200），如需按状态码告警需另行改造。</p>
     *
     * @param e 未识别异常
     * @return 错误结果
     */
    @ExceptionHandler(Throwable.class)
    public CStrResult<Void> handle(Throwable e) {

        log.error("handle Throwable，requestURI: {}", CRequestUtils.getRequestURIDefaultNull(), e);
        return CStrResult.error("未知异常");
    }

}
