package com.c332030.ctool4j.spring.event.listener;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;

/**
 * <p>
 * Description: CApplicationListener
 * </p>
 *
 * @since 2025/10/31
 */
@FunctionalInterface
public interface CApplicationListener<E extends ApplicationEvent> extends ApplicationListener<E> {

    default boolean supports(E event) {
        return true;
    }

    @Override
    default void onApplicationEvent(@NonNull E event) {
        if(supports(event)) {
            onEvent(event);
        }
    }

    void onEvent(E event);

}
