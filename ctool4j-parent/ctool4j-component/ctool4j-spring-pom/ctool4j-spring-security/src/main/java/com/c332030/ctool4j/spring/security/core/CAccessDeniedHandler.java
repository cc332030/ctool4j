package com.c332030.ctool4j.spring.security.core;

import com.c332030.ctool4j.spring.security.util.CSpringSecurityUtils;
import lombok.CustomLog;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Description: CAccessDeniedHandler
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CAccessDeniedHandler}：访问拒绝处理器。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>处理访问被拒绝，输出 403 错误响应</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>输出 403</p>
 * <h2>适用范围</h2>
 * <p>访问拒绝处理</p>
 * <h2>不适用与边界场景</h2>
 * <p>实现 AccessDeniedHandler</p>
 * <h2>已知限制与取舍</h2>
 * <p>实现 AccessDeniedHandler</p>
 *
 * @since 2026/1/28
 * @version 1.0
 */
@CustomLog
public class CAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * 处理访问被拒绝：输出 403 错误响应
     *
     * @param request              请求
     * @param response             响应
     * @param accessDeniedException 访问拒绝异常
     */
    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException
    ) {

        log.debug("CAccessDeniedHandler", accessDeniedException);

        CSpringSecurityUtils.writeJsonError(
            HttpStatus.FORBIDDEN,
            request, response
        );

    }

}
