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

    /**
     * 有的项目中，创建 Feign Client 的时候，会触发这个事件，但是这个时候，Bean 没有生成
     */
    @Override
    default void afterPropertiesSet() {
        onInit();
    }

    /**
     * 刷新上下文时
     */
    @Override
    default void onEvent(ContextRefreshedEvent event) {
        onInit();
    }

    /**
     * 初始化
     */
    void onInit();

}
