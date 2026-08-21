package com.c332030.ctool4j.spring.security.configuration;

import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: CSecurityInit
 * </p>
 *
 * @since 2026/1/24
 * @see doc/design/spring/CSecurityInit.adoc
 */
@Component
public class CSecurityInit implements ICSpringInit {

    /**
     * Spring 启动初始化回调（当前无处理逻辑）
     */
    @Override
    public void onInit() {

    }

}
