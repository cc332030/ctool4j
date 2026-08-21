package com.c332030.ctool4j.spring.lifecycle;

import org.springframework.beans.factory.SmartInitializingSingleton;

/**
 * <p>
 * Description: ICSpringInit
 * </p>
 *
 * @since 2025/11/28
 * @see doc/design/spring/ICSpringInit.adoc
 */
public interface ICSpringInit extends SmartInitializingSingleton {

    /**
     * 单例 Bean 实例化后触发初始化
     */
    @Override
    default void afterSingletonsInstantiated() {
        onInit();
    }

    /**
     * 初始化
     */
    void onInit();

}
