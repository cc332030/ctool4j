package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.core.exception.CException;
import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.exception.annotation.ConditionalOnMissingExceptionHandler;
import lombok.CustomLog;
import lombok.val;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * Description: CCExceptionHandler
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CCExceptionHandler} 标注 {@code @RestControllerAdvice}，通过 {@code @ExceptionHandler(CException.class)} 处理通用异常：</p>
 * <ul>
 *   <li>兜底优先级：{@code @Order(CExceptionHandlerOrder.C_EXCEPTION_FALLBACK)}（兜底区档位，见 {@code CExceptionHandlerOrder}）——Spring 按 advice 顺序取首个能匹配的处理器、不跨 advice 比较异常类型精确度，
 *   故具体类型处理器（{@code @Order(CExceptionHandlerOrder.CONCRETE)}）先被咨询，子类异常（如 {@code CBusinessException}、{@code CUnauthorizedException}）不会被本处理器截走。</li>
 *   <li>通过 {@code @ConditionalOnMissingExceptionHandler(CException.class)} 控制：容器存在其他同类型处理器时本处理器不生效。</li>
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
 *     <td>不取异常内容，返回「通用异常」或异常类型名，避免 message: null</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>Web 应用统一通用异常的响应格式。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>依赖 {@code CStrResult} 响应结构，与项目统一响应格式耦合。</li>
 * </ul>
 *
 * @since 2026/4/9
 * @version 1.3
 */
@CustomLog
@Order(CExceptionHandlerOrder.C_EXCEPTION_FALLBACK)
@RestControllerAdvice
@ConditionalOnMissingExceptionHandler(CException.class)
public class CCExceptionHandler {

    /**
     * 处理通用异常
     * <p>{@code e} 为 null 属非预期入参（Spring MVC 命中 {@code @ExceptionHandler} 时异常对象不为 null），
     * 这里兜底返回固定错误结果（不读异常内容）；见类 javadoc「兜底设计」。</p>
     * <p>异常消息为 null（如 {@code new CException((String) null)}）时用异常类型简单名兜底，空消息（非 null）原样返回。</p>
     *
     * @param e 通用异常
     * @return 错误结果
     */
    @ExceptionHandler(CException.class)
    public CStrResult<Void> handle(CException e) {

        if (null == e) {
            return CStrResult.error("通用异常");
        }

        log.debug("handle CException，requestURI: {}", CRequestUtils.getRequestURIDefaultNull(), e);

        val message = e.getMessage();
        if (null == message) {
            return CStrResult.error(e.getClass().getSimpleName());
        }

        return CStrResult.error(message);
    }

}
