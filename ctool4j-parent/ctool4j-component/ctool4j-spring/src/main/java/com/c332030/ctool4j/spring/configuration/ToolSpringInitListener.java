package com.c332030.ctool4j.spring.configuration;

import com.c332030.ctool4j.spring.bean.CSpringConfigBeans;
import com.c332030.ctool4j.spring.config.CSpringApplicationConfig;
import com.c332030.ctool4j.spring.event.listener.CurrentContextRefreshedListener;
import com.c332030.ctool4j.spring.util.CSpringUtils;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: ToolSpringInitListener
 * </p>
 *
 * @since 2025/11/10
 */
@Component
public class ToolSpringInitListener implements CurrentContextRefreshedListener {

    @Override
    public void onEvent(ContextRefreshedEvent event) {
        CSpringUtils.wireBean(CSpringApplicationConfig.class, CSpringConfigBeans::setSpringApplicationConfig);
    }

}
