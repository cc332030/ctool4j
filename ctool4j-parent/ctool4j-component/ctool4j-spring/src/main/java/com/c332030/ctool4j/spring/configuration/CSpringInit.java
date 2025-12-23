package com.c332030.ctool4j.spring.configuration;

import com.c332030.ctool4j.definition.constant.CToolConstants;
import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import com.c332030.ctool4j.spring.util.CAutowiredUtils;
import com.c332030.ctool4j.spring.util.CSpringUtils;
import lombok.CustomLog;
import lombok.val;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: CSpringInit
 * </p>
 *
 * @since 2025/11/10
 */
@CustomLog
@Component
public class CSpringInit implements ICSpringInit {

    @Override
    public void onInit() {

        CAutowiredUtils.autowired(CToolConstants.BASE_PACKAGE);

        val basePackages = CSpringUtils.getBasePackages();
        log.info("basePackages: {}", basePackages);
        basePackages.forEach(CAutowiredUtils::autowired);

    }

}
