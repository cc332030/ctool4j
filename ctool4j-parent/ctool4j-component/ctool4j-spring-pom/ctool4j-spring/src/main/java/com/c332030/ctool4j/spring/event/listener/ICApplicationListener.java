package com.c332030.ctool4j.spring.event.listener;

import com.c332030.ctool4j.definition.interfaces.ICEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;

/**
 * <p>
 * Description: ICApplicationListener
 * </p>
 *
 * @since 2025/10/31
 * @see doc/design/spring/ICApplicationListener.adoc
 */
@FunctionalInterface
public interface ICApplicationListener<E extends ApplicationEvent> extends ApplicationListener<E>, ICEvent<E> {

    /**
     * 是否支持处理该事件
     * @param event 事件
     * @return 是否支持
     */
    default boolean supports(E event) {
        return true;
    }

    /**
     * 处理事件（支持时委托给 onEvent）
     * @param event 事件
     */
    @Override
    default void onApplicationEvent(@NonNull E event) {
        if(supports(event)) {
            onEvent(event);
        }
    }

    /**
     * 处理事件
     * @param event 事件
     */
    void onEvent(E event);

}
