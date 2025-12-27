package com.c332030.ctool4j.feign.configuration;

import com.c332030.ctool4j.feign.util.CFeignUtils;
import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import com.c332030.ctool4j.spring.util.CAutowiredUtils;
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

        CAutowiredUtils.autowired(CFeignUtils.class);

    }

}
