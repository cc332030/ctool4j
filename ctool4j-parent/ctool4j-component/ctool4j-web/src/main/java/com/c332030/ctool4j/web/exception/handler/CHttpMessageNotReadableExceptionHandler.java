package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.exception.annotation.ConditionalOnMissingExceptionHandler;
import lombok.CustomLog;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * Description: CHttpMessageNotReadableExceptionHandler
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>通过 {@code @ConditionalOnMissingExceptionHandler(HttpMessageNotReadableException.class)} 控制：容器存在其他同类型处理器时本处理器不生效。</li>
 *   <li>返回 {@code CStrResult&lt;Void&gt;} 错误结果（固定文案，不透出异常内部消息）。</li>
 *   <li>记录请求 URI 与异常堆栈（log），保证问题可追溯。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>为什么用固定文案</b></p>
 * <ul>
 *   <li>{@link HttpMessageNotReadableException} 自身消息对客户端既不可读、又含服务端内部信息：请求体缺失时是
 *   控制器方法签名（{@code Required request body is missing: public ...AuthWeEmployeeController.sidebarLogin(...)}），
 *   JSON 解析失败时是解析器细节与嵌套异常。直接透出会泄露内部结构，故统一返回可读文案。</li>
 * </ul>
 * <p><b>为什么是 debug 级日志</b></p>
 * <ul>
 *   <li>请求体缺失/格式错误属客户端问题，用 {@code log.debug} 记录以免污染服务端错误日志（与兜底的
 *   {@link CThrowableHandler} 用 {@code log.error} 区分）。</li>
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
 *   <tr>
 *     <td>其他请求体不可读情形（格式错误、类型不匹配）</td>
 *     <td>同一处理器覆盖，返回同一文案</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>Web 应用统一「请求体缺失 / 不可读」异常的响应格式（{@code @RequestBody} 必填但未传、JSON 语法或类型不合法）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不区分具体原因（缺失 / 格式错误 / 类型不匹配）：如需按原因区分响应，由业务自定义同类型处理器覆盖。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>依赖 {@code CStrResult} 响应结构，与项目统一响应格式耦合。</li>
 *   <li>业务码沿用同目录既有处理器的约定（{@code CStrResult.error(String)} → 500），未按 400 区分客户端错误；
 *   如需与 HTTP 语义对齐，可改用 {@code CStrResult.error(HttpStatus.BAD_REQUEST, ...)}。</li>
 * </ul>
 *
 * @since 2026/9/14
 * @version 1.0
 */
@CustomLog
@RestControllerAdvice
@ConditionalOnMissingExceptionHandler(HttpMessageNotReadableException.class)
public class CHttpMessageNotReadableExceptionHandler {

    /**
     * 处理请求体不可读异常
     *
     * @param e 请求体不可读异常
     * @return 错误结果
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public CStrResult<Void> handle(HttpMessageNotReadableException e) {

        log.debug("handle HttpMessageNotReadableException，requestURI: {}",
            CRequestUtils.getRequestURIDefaultNull(), e);
        return CStrResult.error("请求体缺失或格式不正确");
    }

}
