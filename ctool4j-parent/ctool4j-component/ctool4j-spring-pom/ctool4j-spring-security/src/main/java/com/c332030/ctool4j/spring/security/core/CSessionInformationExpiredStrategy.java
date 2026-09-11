package com.c332030.ctool4j.spring.security.core;

import com.c332030.ctool4j.spring.security.util.CSpringSecurityUtils;
import lombok.CustomLog;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

/**
 * <p>
 * Description: CSessionInformationExpiredStrategy
 * </p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>处理会话过期，输出 401</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>输出 401</p>
 * <h2>适用范围</h2>
 * <p>会话过期处理</p>
 *
 * @since 2026/1/28
 * @version 1.0
 */
@CustomLog
public class CSessionInformationExpiredStrategy implements SessionInformationExpiredStrategy {

    /**
     * 会话过期处理：输出 401 错误响应
     *
     * @param event 会话过期事件
     */
    @Override
    public void onExpiredSessionDetected(
        SessionInformationExpiredEvent event
    ) {

        log.debug("CSessionInformationExpiredStrategy");

        CSpringSecurityUtils.writeJsonError(
            HttpStatus.UNAUTHORIZED,
            "Expired",
            event.getRequest(), event.getResponse()
        );

    }

}
