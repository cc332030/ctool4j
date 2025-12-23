package com.c332030.ctool4j.log.configuration;

import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * Description: CLogInit
 * </p>
 *
 * @since 2025/9/29
 */
@Configuration
@AllArgsConstructor
public class CLogInit implements ICSpringInit {

    @Override
    public void onInit() {
//        CSpringUtils.wireBean(CRequestLogConfig.class, CRequestLogUtils::setRequestLogConfig);
    }

}
