package com.c332030.ctool4j.spring.event.listener;

import com.c332030.ctool4j.spring.util.CSpringUtils;
import org.springframework.context.ApplicationEvent;

/**
 * <p>
 * Description: ICSpringSourceApplicationListener
 * </p>
 *
 * @since 2025/10/31
 */
@FunctionalInterface
public interface ICSpringSourceApplicationListener<T extends ApplicationEvent> extends ICApplicationListener<T> {

    /**
     * 是否支持当前上下文事件
     * @param event 事件
     * @return 是否支持
     */
    @Override
    default boolean supports(T event) {
        return CSpringUtils.isCurrentContextEvent(event);
    }

}
