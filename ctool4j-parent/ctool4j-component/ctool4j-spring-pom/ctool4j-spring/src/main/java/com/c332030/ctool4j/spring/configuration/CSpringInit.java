package com.c332030.ctool4j.spring.configuration;

import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: CSpringInit
 * </p>
 *
 * @since 2025/11/10
 * @see "doc/design/spring/CSpringInit.adoc"
 */
@CustomLog
@Component
public class CSpringInit implements ICSpringInit {

    /**
     * Spring 启动初始化回调（当前无处理逻辑）
     */
    @Override
    public void onInit() {

    }

}
