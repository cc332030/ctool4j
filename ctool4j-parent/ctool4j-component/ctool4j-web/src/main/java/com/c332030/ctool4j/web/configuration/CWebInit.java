package com.c332030.ctool4j.web.configuration;

import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: CWebInit
 * </p>
 *
 * @since 2026/1/9
 * @see "doc/design/web/CWebInit.adoc"
 */
@Component
public class CWebInit implements ICSpringInit {

    /**
     * Spring 启动初始化回调（当前无处理逻辑）
     */
    @Override
    public void onInit() {

    }

}
