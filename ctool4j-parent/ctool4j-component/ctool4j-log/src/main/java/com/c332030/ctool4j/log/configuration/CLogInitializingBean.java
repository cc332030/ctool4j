package com.c332030.ctool4j.log.configuration;

import com.c332030.ctool4j.log.config.CRequestLogConfig;
import com.c332030.ctool4j.log.util.CRequestLogUtils;
import com.c332030.ctool4j.spring.util.CSpringUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * Description: CLogInitializingBean
 * </p>
 *
 * @since 2025/9/29
 */
@Configuration
@AllArgsConstructor
public class CLogInitializingBean implements InitializingBean {

    @Override
    public void afterPropertiesSet() {

        CSpringUtils.wireBean(CRequestLogConfig.class, CRequestLogUtils::setRequestLogConfig);

    }

}
