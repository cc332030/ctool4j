package com.c332030.ctool4j.spring.security.configuration;

import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import com.c332030.ctool4j.spring.security.util.CAuthenticationUtils;
import com.c332030.ctool4j.spring.util.CAutowiredUtils;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: CSecurityInit
 * </p>
 *
 * @since 2026/1/24
 */
@Component
public class CSecurityInit implements ICSpringInit {

    @Override
    public void onInit() {
        CAutowiredUtils.autowired(CAuthenticationUtils.class);
    }

}
