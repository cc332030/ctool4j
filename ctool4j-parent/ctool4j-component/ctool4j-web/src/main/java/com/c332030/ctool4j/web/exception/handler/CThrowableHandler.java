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
 * @since 2026/4/9
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
