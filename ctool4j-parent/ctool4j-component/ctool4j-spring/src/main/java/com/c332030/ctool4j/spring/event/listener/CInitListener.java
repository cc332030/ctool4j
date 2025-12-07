package com.c332030.ctool4j.spring.event.listener;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * <p>
 * Description: CInitListener
 * </p>
 *
 * @since 2025/11/28
 */
public interface CInitListener extends InitializingBean, CurrentContextRefreshedListener {

    @Override
    default void afterPropertiesSet() {
        onInit();
    }

    @Override
    default void onEvent(ContextRefreshedEvent event) {
        onInit();
    }

    /**
     * 初始化
     */
    void onInit();

}
