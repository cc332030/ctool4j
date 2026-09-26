package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.exception.annotation.ConditionalOnMissingExceptionHandler;
import lombok.CustomLog;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * Description: CThrowableHandler
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CThrowableHandler} 标注 {@code @RestControllerAdvice}，通过 {@code @ExceptionHandler(Throwable.class)} 处理
 * <b>未被识别</b>的异常（见 {@code CExceptionHandlerOrder} 的档位说明），职责只有「纯粹的返回」——不识别类型、不委托他人：</p>
 * <ul>
 *   <li>兜底优先级：{@code @Order(CExceptionHandlerOrder.THROWABLE_FALLBACK)}（兜底区最后一档）——Spring 按 advice 顺序取首个能匹配的处理器、不跨 advice 比较异常类型精确度，
 *   故具体类型处理器、{@code CException} 兜底与 {@code CExceptionHandler} 均先被咨询，本处理器只在无其他匹配时兜底。</li>
 *   <li>返回固定「未知异常」错误结果（业务码 500）：{@code CStrResult.error(UNKNOWN_MESSAGE)}。</li>
 *   <li>通过 {@code @ConditionalOnMissingExceptionHandler(Throwable.class)} 控制：容器存在其他同类型处理器时本处理器不生效。</li>
 *   <li>记录请求 URI 与异常堆栈（log.error），保证问题可追溯。</li>
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
 *     <td>不取异常内容，返回固定「未知异常」错误结果，日志降为 debug</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>Web 应用统一<b>未被识别</b>异常的响应格式。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不承担类型识别与委托：上传超限（含容器私有类型）的识别与响应已由 {@code CExceptionHandler} 承接，
 *   本处理器只在它也不匹配时兜底（典型为 {@code Error} 及其子类）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>依赖 {@code CStrResult} 响应结构，与项目统一响应格式耦合。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>用 {@code CRequestUtils.getRequestURIDefaultNull()} 记录请求 URI 用于日志。</li>
 *   <li>{@code UNKNOWN_MESSAGE} 为公开常量：与本档职责相关的响应文案由 {@code CExceptionHandler} 共用，避免同一语义两处漂移。</li>
 * </ul>
 *
 * @since 2026/4/9
 * @version 1.5
 */
@CustomLog
@Order(CExceptionHandlerOrder.THROWABLE_FALLBACK)
@RestControllerAdvice
@ConditionalOnMissingExceptionHandler(Throwable.class)
public class CThrowableHandler {

    /**
     * 未识别异常的固定响应文案（本处理器与 {@code CExceptionHandler} 共用，保证同一语义只有一处取值）
     */
    public static final String UNKNOWN_MESSAGE = "未知异常";

    /**
     * 兜底处理未识别异常
     * <p>有意设计：捕获并完整记录异常（log.error 带堆栈），保证问题可追溯、异常不穿透到容器默认错误页；
     * 统一返回 200 业务 JSON 保持响应结构一致。已知边界：HTTP 语义缺失，无法按 5xx 触发告警
     * （含 OOM、StackOverflow 等 Error 也返回 200），如需按状态码告警需另行改造。</p>
     *
     * @param e 未识别异常（为 null 属非预期入参，兜底不取异常内容、返回固定「未知异常」；见类 javadoc「兜底设计」）
     * @return 错误结果
     */
    @ExceptionHandler(Throwable.class)
    public CStrResult<Void> handle(Throwable e) {

        // null 属非预期入参（Spring MVC 命中 @ExceptionHandler 时异常对象不为 null）：不取异常内容、日志降为 debug，
        // 返回固定「未知异常」，避免为不可达分支引入二次 NPE
        if (null == e) {
            log.debug("handle null Throwable，requestURI: {}", CRequestUtils.getRequestURIDefaultNull());
            return CStrResult.error(UNKNOWN_MESSAGE);
        }

        log.error("handle Throwable，requestURI: {}", CRequestUtils.getRequestURIDefaultNull(), e);
        return CStrResult.error(UNKNOWN_MESSAGE);
    }

}
