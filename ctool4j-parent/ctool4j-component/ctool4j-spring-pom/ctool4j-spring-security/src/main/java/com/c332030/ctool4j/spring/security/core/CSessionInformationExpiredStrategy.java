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
 */
@CustomLog
public class CSessionInformationExpiredStrategy implements SessionInformationExpiredStrategy {

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
