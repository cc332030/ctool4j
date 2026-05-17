package com.c332030.ctool4j.spring.configuration;

import com.c332030.ctool4j.core.jackson.CJacksonUtils;
import com.c332030.ctool4j.core.util.CBoolUtils;
import com.c332030.ctool4j.spring.config.CSpringJacksonConfig;
import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * Description: CJacksonInit
 * </p>
 *
 * @since 2026/4/8
 */
@Configuration
@AllArgsConstructor
public class CJacksonInit implements ICSpringInit {

    CSpringJacksonConfig jacksonConfig;

    ObjectMapper objectMapper;

    @Override
    public void onInit() {

        if(CBoolUtils.isTrue(jacksonConfig.getJson5())) {
            CJacksonUtils.configure(objectMapper);
        }

    }

}
