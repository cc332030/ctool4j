package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.exception.annotation.ConditionalOnMissingExceptionHandler;
import lombok.CustomLog;
import lombok.val;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * Description: CMissingServletRequestParameterExceptionHandler
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>通过 {@code @ConditionalOnMissingExceptionHandler(MissingServletRequestParameterException.class)} 控制：容器存在其他同类型处理器时本处理器不生效。</li>
 *   <li>返回 {@code CStrResult&lt;Void&gt;} 错误结果（文案保留缺失的参数名）。</li>
 *   <li>记录请求 URI 与异常堆栈（log），保证问题可追溯。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>为什么不用异常自身消息</b></p>
 * <ul>
 *   <li>异常消息形如 {@code Required request parameter 'id' for method parameter type java.lang.Long is not present}：
 *   含 Java 类型名、且为英文框架文案；本处理器改为「中文文案 + 参数名」，对调用方更可读。</li>
 *   <li>保留参数名是有意取舍：参数名是调用方唯一能据此修正请求的信息（口径与
 *   {@link CMethodArgumentNotValidExceptionHandler} 拼接字段名一致），其余内部细节不对外。</li>
 * </ul>
 * <p><b>为什么是 debug 级日志</b></p>
 * <ul>
 *   <li>必填参数缺失属客户端问题，用 {@code log.debug} 记录以免污染服务端错误日志（与兜底的
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
 *     <td>异常对象为 null（非预期，兜底不依赖异常内容）</td>
 *     <td>不取异常内容，返回固定「缺少必填参数」错误结果</td>
 *   </tr>
 *   <tr>
 *     <td>参数名不可得</td>
 *     <td>按异常自身取值拼接（取值由框架解析阶段决定，本处理器不做兜底改写）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>Web 应用统一「必填请求参数未传」异常的响应格式（{@code @RequestParam(required = true)} 未传等）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不覆盖其余请求绑定异常（如必填请求头缺失 {@code MissingRequestHeaderException}），如需要可由业务自定义处理器覆盖。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>依赖 {@code CStrResult} 响应结构，与项目统一响应格式耦合。</li>
 *   <li>业务码沿用同目录既有处理器的约定（{@code CStrResult.error(String)} → 500），未按 400 区分客户端错误。</li>
 * </ul>
 *
 * @since 2026/9/14
 * @version 1.2
 */
@CustomLog
@Order(CExceptionHandlerOrder.CONCRETE)
@RestControllerAdvice
@ConditionalOnMissingExceptionHandler(MissingServletRequestParameterException.class)
public class CMissingServletRequestParameterExceptionHandler {

    /**
     * 处理必填请求参数缺失异常
     * <p>{@code e} 为 null 属非预期入参（Spring MVC 命中 {@code @ExceptionHandler} 时异常对象不为 null），
     * 这里兜底返回固定错误结果（不读 {@code getParameterName()} 等异常内容）；见类 javadoc「兜底设计」。
     * 参数名为 null 时只返回固定文案、不拼 {@code null}。</p>
     *
     * @param e 必填请求参数缺失异常
     * @return 错误结果
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public CStrResult<Void> handle(MissingServletRequestParameterException e) {

        log.debug("handle MissingServletRequestParameterException，requestURI: {}",
            CRequestUtils.getRequestURIDefaultNull(), e);

        if (null == e) {
            return CStrResult.error("缺少必填参数");
        }

        val parameterName = e.getParameterName();
        if (null == parameterName) {
            return CStrResult.error("缺少必填参数");
        }

        return CStrResult.error("缺少必填参数：" + parameterName);
    }

}
