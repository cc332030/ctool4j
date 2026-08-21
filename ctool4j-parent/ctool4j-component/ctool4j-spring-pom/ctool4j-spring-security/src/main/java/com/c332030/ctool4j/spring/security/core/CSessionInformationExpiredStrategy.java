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
 * @since 2026/1/28
 * @see "doc/design/spring/CSessionInformationExpiredStrategy.adoc"
 * @see "doc/design/spring/CSessionInformationExpiredStrategyTests.adoc"
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
