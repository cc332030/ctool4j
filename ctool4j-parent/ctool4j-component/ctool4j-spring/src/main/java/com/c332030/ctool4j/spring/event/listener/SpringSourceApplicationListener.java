package com.c332030.ctool4j.spring.event.listener;

import com.c332030.ctool4j.spring.util.CSpringUtils;
import org.springframework.context.ApplicationEvent;

/**
 * <p>
 * Description: SpringSourceApplicationListener
 * </p>
 *
 * @since 2025/10/31
 */
@FunctionalInterface
public interface SpringSourceApplicationListener<T extends ApplicationEvent> extends CApplicationListener<T> {

    @Override
    default boolean supports(T event) {
        return CSpringUtils.isCurrentContextEvent(event);
    }

}
