package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.core.exception.CBusinessException;
import com.c332030.ctool4j.core.util.CResUtils;
import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.exception.annotation.ConditionalOnMissingExceptionHandler;
import lombok.CustomLog;
import lombok.val;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * Description: CCBusinessExceptionHandler
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>通过 {@code @ConditionalOnMissingExceptionHandler(CBusinessException.class)} 控制：容器存在其他同类型处理器时本处理器不生效。</li>
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
 *   <li>Web 应用统一业务异常的响应格式。</li>
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
@ConditionalOnMissingExceptionHandler(CBusinessException.class)
public class CCBusinessExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @return 错误结果
     */
    @ExceptionHandler(CBusinessException.class)
    public CStrResult<Void> handle(CBusinessException e) {

        log.debug("handle CBusinessException，requestURI: {}", CRequestUtils.getRequestURIDefaultNull(), e);

        val error = e.getError();
        if(null == error) {
            return CStrResult.error(e.getMessage());
        }

        val msgExtend = e.getMsgExtend();

        return CStrResult.error(
            String.valueOf(error.getCode()),
            CResUtils.formatMessage(error, msgExtend)
        );
    }

}
