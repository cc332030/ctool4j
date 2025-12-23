package com.c332030.ctool4j.feign.configuration;

import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: CFeignInit
 * </p>
 *
 * @since 2025/12/22
 */
@Component
public class CFeignInit implements ICSpringInit {

    @Override
    public void onInit() {

//        CSpringUtils.wireBean(CFeignClientHeaderConfig.class, CFeignUtils.class);

    }

}
