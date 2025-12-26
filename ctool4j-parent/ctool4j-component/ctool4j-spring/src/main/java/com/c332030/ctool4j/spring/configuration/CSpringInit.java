package com.c332030.ctool4j.spring.configuration;

import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import com.c332030.ctool4j.spring.util.CAutowiredUtils;
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

        CAutowiredUtils.listAnnotatedClassThenDo(CAutowired.class, CAutowiredUtils::autowired);

    }

}
