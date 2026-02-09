package com.c332030.ctool4j.spring.configuration;

import com.c332030.ctool4j.spring.bean.CSpringConfigBeans;
import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import com.c332030.ctool4j.spring.util.CAutowiredUtils;
import com.c332030.ctool4j.spring.util.CSpringHttpUtils;
import lombok.CustomLog;
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

        CAutowiredUtils.autowired(CSpringConfigBeans.class);
        CAutowiredUtils.autowired(CSpringHttpUtils.class);

    }

}
