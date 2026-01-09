package com.c332030.ctool4j.web.configuration;

import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import com.c332030.ctool4j.spring.util.CAutowiredUtils;
import com.c332030.ctool4j.web.cors.util.CCorsUtils;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: CWebInit
 * </p>
 *
 * @since 2026/1/9
 */
@Component
public class CWebInit implements ICSpringInit {

    @Override
    public void onInit() {
        CAutowiredUtils.autowired(CCorsUtils.class);
    }

}
