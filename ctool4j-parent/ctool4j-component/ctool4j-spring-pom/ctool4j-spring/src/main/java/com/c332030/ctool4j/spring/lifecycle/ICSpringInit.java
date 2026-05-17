package com.c332030.ctool4j.spring.lifecycle;

import org.springframework.beans.factory.SmartInitializingSingleton;

/**
 * <p>
 * Description: ICSpringInit
 * </p>
 *
 * @since 2025/11/28
 */
public interface ICSpringInit extends SmartInitializingSingleton {

    @Override
    default void afterSingletonsInstantiated() {
        onInit();
    }

    /**
     * 初始化
     */
    void onInit();

}
