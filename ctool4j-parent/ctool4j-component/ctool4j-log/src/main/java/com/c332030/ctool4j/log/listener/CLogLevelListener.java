package com.c332030.ctool4j.log.listener;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.log.CLogUtils;
import com.c332030.ctool4j.spring.event.listener.ICApplicationListener;
import com.c332030.ctool4j.spring.exception.annotation.CCatchAndLogThrowable;
import com.c332030.ctool4j.spring.service.ICProxyService;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.val;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.core.env.Environment;

import java.util.stream.Collectors;

/**
 * <p>
 * Description: CLogLevelListener
 * </p>
 *
 * @since 2025/12/21
 */
@CustomLog
//@Component // 好像已经支持了
@AllArgsConstructor
public class CLogLevelListener implements ICApplicationListener<EnvironmentChangeEvent>, ICProxyService<CLogLevelListener> {

    Environment environment;

    @Override
    public void onEvent(EnvironmentChangeEvent event) {

        val keys = event.getKeys().stream()
            .filter(key -> key.startsWith(CLogUtils.LOGGING_LEVEL))
            .collect(Collectors.toList());

        if(CollUtil.isEmpty(keys)) {
            log.debug("keys is empty");
            return;
        }

        keys.forEach(currentProxy()::updateLogLevel);

    }

    @CCatchAndLogThrowable
    public void updateLogLevel(String key) {

        val className = StrUtil.subSuf(key, CLogUtils.LOGGING_LEVEL_PREFIX.length());
        if(StrUtil.isBlank(className)) {
            return;
        }
        val levelNew = environment.getProperty(key);
        if(StrUtil.isBlank(levelNew)) {
            return;
        }

        val logger = LoggerFactory.getLogger(className);
        if(logger instanceof Logger) {
            updateLogbackLevel((Logger) logger, levelNew);
        }

    }

    private void updateLogbackLevel(Logger logger, String levelStrNew) {

        val levelNew = Level.toLevel(levelStrNew);
        log.info("update logback level, logger: {}, {} -> {}",
            logger.getName(), logger.getLevel(), levelNew
        );
        logger.setLevel(levelNew);

    }

}
