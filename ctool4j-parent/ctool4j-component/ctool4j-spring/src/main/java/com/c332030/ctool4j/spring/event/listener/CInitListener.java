package com.c332030.ctool4j.spring.event.listener;

import org.springframework.beans.factory.SmartInitializingSingleton;

/**
 * <p>
 * Description: CInitListener
 * </p>
 *
 * @since 2025/11/28
 */
public interface CInitListener extends SmartInitializingSingleton {

    @Override
    default void afterSingletonsInstantiated() {
        onInit();
    }

    /**
     * 初始化
     */
    void onInit();

}
