package com.c332030.ctool4j.spring.configuration;

import com.c332030.ctool4j.spring.bean.CSpringConfigBeans;
import com.c332030.ctool4j.spring.config.CSpringApplicationConfig;
import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import com.c332030.ctool4j.spring.util.CSpringUtils;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: CSpringInit
 * </p>
 *
 * @since 2025/11/10
 */
@Component
public class CSpringInit implements ICSpringInit {

    @Override
    public void onInit() {
        CSpringUtils.wireBean(CSpringApplicationConfig.class, CSpringConfigBeans::setSpringApplicationConfig);
    }

}
