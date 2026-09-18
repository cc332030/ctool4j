package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.exception.annotation.ConditionalOnMissingExceptionHandler;
import lombok.CustomLog;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * Description: CClientAbortExceptionHandler
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>通过 {@code @ConditionalOnMissingExceptionHandler(valueName = ...)} 控制：容器存在其他同类型处理器时本处理器不生效。</li>
 *   <li>通过 {@code @ConditionalOnClass(name = ...)} 控制：仅在存在 Tomcat 的 {@code ClientAbortException} 时装配，Jetty/Undertow 下自动跳过。</li>
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
 *     <td>异常对象为 null（非预期，兜底不依赖异常内容）</td>
 *     <td>不取异常内容，仅记录请求 URI（无响应体）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>Web 应用统一客户端中止异常的响应格式。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>仅处理 Tomcat（含 Spring Boot 内嵌 Tomcat）下的客户端中断：该异常为容器专有类型，Jetty/Undertow 下本处理器不装配。</li>
 *   <li>注解只引用类名字符串（{@code @ConditionalOnClass(name)} + {@code valueName}）：不解析容器私有类，
 *   避免在缺失该类的环境读取注解属性时类加载失败；方法签名仍声明该类型，但被条件排除时不会被解析。</li>
 * </ul>
 *
 * @since 2026/4/9
 * @version 1.3
 */
@CustomLog
@Order(CExceptionHandlerOrder.CONCRETE)
@RestControllerAdvice
@ConditionalOnClass(name = CClientAbortExceptionHandler.CLIENT_ABORT_EXCEPTION_CLASS_NAME)
@ConditionalOnMissingExceptionHandler(valueName = CClientAbortExceptionHandler.CLIENT_ABORT_EXCEPTION_CLASS_NAME)
public class CClientAbortExceptionHandler {

    /**
     * 客户端中断异常的全限定类名（Tomcat 专有类型；注解按类名引用，避免无该类环境解析失败）
     */
    public static final String CLIENT_ABORT_EXCEPTION_CLASS_NAME = "org.apache.catalina.connector.ClientAbortException";

    /**
     * 处理客户端连接中断异常，仅记录日志
     *
     * @param e 客户端连接中断异常（为 null 属非预期入参，仅记日志、不写响应；见类 javadoc「兜底设计」）
     */
    @ExceptionHandler(ClientAbortException.class)
    public void handle(ClientAbortException e) {

        // null 属非预期入参（Spring MVC 命中 @ExceptionHandler 时异常对象不为 null）：不取异常内容、直接返回
        if (null == e) {
            log.debug("handle null ClientAbortException，requestURI: {}", CRequestUtils.getRequestURIDefaultNull());
            return;
        }

        log.debug("handle ClientAbortException，requestURI: {}", CRequestUtils.getRequestURIDefaultNull(), e);
    }

}
