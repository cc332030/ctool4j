package com.c332030.ctool4j.log.configuration;

import com.c332030.ctool4j.log.config.CRequestLogConfig;
import com.c332030.ctool4j.log.util.CRequestLogUtils;
import com.c332030.ctool4j.spring.event.listener.CInitListener;
import com.c332030.ctool4j.spring.util.CSpringUtils;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * Description: CLogInitListener
 * </p>
 *
 * @since 2025/9/29
 */
@Configuration
@AllArgsConstructor
public class CLogInitListener implements CInitListener {

    @Override
    public void onInit() {
        CSpringUtils.wireBean(CRequestLogConfig.class, CRequestLogUtils::setRequestLogConfig);
    }

}
