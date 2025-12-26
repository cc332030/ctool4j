package com.c332030.ctool4j.spring.bean;

import com.c332030.ctool4j.spring.config.CSpringApplicationConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

/**
 * <p>
 * Description: CSpringConfigBeans
 * </p>
 *
 * @since 2025/11/10
 */
@UtilityClass
public class CSpringConfigBeans {

    @Getter
    @Setter
    CSpringApplicationConfig springApplicationConfig;

}
