package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.exception.annotation.ConditionalOnMissingExceptionHandler;
import lombok.CustomLog;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * <p>
 * Description: CMethodArgumentTypeMismatchExceptionHandler
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>通过 {@code @ConditionalOnMissingExceptionHandler(MethodArgumentTypeMismatchException.class)} 控制：容器存在其他同类型处理器时本处理器不生效。</li>
 *   <li>返回 {@code CStrResult&lt;Void&gt;} 错误结果（文案保留出错的参数名）。</li>
 *   <li>记录请求 URI 与异常堆栈（log），保证问题可追溯。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>为什么不用异常自身消息</b></p>
 * <ul>
 *   <li>异常消息形如 {@code Failed to convert value of type 'java.lang.String' to required type 'java.lang.Integer'}，
 *   含全限定 Java 类型名与嵌套异常细节，属服务端内部信息；本处理器改为「中文文案 + 参数名」。</li>
 *   <li>保留参数名是有意取舍：参数名是调用方唯一能据此修正请求的信息（口径与
 *   {@link CMethodArgumentNotValidExceptionHandler} 拼接字段名一致），其余内部细节不对外。</li>
 * </ul>
 * <p><b>为什么是 debug 级日志</b></p>
 * <ul>
 *   <li>参数类型不正确属客户端问题，用 {@code log.debug} 记录以免污染服务端错误日志（与兜底的
 *   {@link CThrowableHandler} 用 {@code log.error} 区分）。</li>
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
 *     <td>参数名不可得</td>
 *     <td>按异常自身取值拼接（取值由框架解析阶段决定，本处理器不做兜底改写）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>Web 应用统一「方法参数类型不匹配」异常的响应格式（如 {@code Long} 参数传了非数字等转换失败场景）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不覆盖请求体内部的类型不匹配（如 JSON 字段类型错误，由 {@link CHttpMessageNotReadableExceptionHandler} 覆盖）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>依赖 {@code CStrResult} 响应结构，与项目统一响应格式耦合。</li>
 *   <li>业务码沿用同目录既有处理器的约定（{@code CStrResult.error(String)} → 500），未按 400 区分客户端错误。</li>
 *   <li>参数名取自异常自身（框架解析阶段命名），不额外解析参数注解，故文案中的名称与框架判定保持一致。</li>
 * </ul>
 *
 * @since 2026/9/14
 * @version 1.1
 */
@CustomLog
@Order(CExceptionHandlerOrder.CONCRETE)
@RestControllerAdvice
@ConditionalOnMissingExceptionHandler(MethodArgumentTypeMismatchException.class)
public class CMethodArgumentTypeMismatchExceptionHandler {

    /**
     * 处理方法参数类型不匹配异常
     *
     * @param e 方法参数类型不匹配异常
     * @return 错误结果
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public CStrResult<Void> handle(MethodArgumentTypeMismatchException e) {

        log.debug("handle MethodArgumentTypeMismatchException，requestURI: {}",
            CRequestUtils.getRequestURIDefaultNull(), e);
        return CStrResult.error("参数类型不正确：" + e.getName());
    }

}
