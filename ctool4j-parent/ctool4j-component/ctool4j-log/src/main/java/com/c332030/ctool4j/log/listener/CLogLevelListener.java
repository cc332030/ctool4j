package com.c332030.ctool4j.log.listener;

import com.c332030.ctool4j.spring.event.listener.ICApplicationListener;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: CLogLevelListener
 * </p>
 *
 * @since 2025/12/21
 */
@CustomLog
@Component
@AllArgsConstructor
public class CLogLevelListener implements ICApplicationListener<EnvironmentChangeEvent> {

    @Override
    public void onEvent(EnvironmentChangeEvent event) {

        log.info("keys: {}", event.getKeys());

    }

}
