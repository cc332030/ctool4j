package com.c332030.ctool4j.spring.configuration;

import com.c332030.ctool4j.spring.bean.CSpringConfigBeans;
import com.c332030.ctool4j.spring.config.CSpringApplicationConfig;
import com.c332030.ctool4j.spring.util.CSpringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: CToolSpringInitializingBean
 * </p>
 *
 * @since 2025/11/10
 */
@Component
public class CToolSpringInitializingBean implements InitializingBean {

    @Override
    public void afterPropertiesSet() {

        CSpringUtils.wireBean(CSpringApplicationConfig.class, CSpringConfigBeans::setSpringApplicationConfig);

    }

}
