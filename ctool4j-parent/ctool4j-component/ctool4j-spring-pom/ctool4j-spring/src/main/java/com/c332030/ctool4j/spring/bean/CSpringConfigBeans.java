package com.c332030.ctool4j.spring.bean;

import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import com.c332030.ctool4j.spring.config.CSpringApplicationConfig;
import lombok.Getter;
import lombok.experimental.UtilityClass;

/**
 * <p>
 * Description: CSpringConfigBeans
 * </p>
 *
 * @since 2025/11/10
 */
@UtilityClass
@CAutowiredScan
public class CSpringConfigBeans {

    @Getter
    @CAutowired
    CSpringApplicationConfig springApplicationConfig;

}
