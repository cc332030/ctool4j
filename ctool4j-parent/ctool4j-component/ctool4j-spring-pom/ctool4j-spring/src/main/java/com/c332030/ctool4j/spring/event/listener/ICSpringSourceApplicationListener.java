package com.c332030.ctool4j.spring.event.listener;

import com.c332030.ctool4j.spring.util.CSpringUtils;
import org.springframework.context.ApplicationEvent;

/**
 * <p>
 * Description: ICSpringSourceApplicationListener
 * </p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>监听 spring 源事件</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>事件监听</p>
 * <h2>不适用与边界场景</h2>
 * <p>继承 ApplicationListener</p>
 * <h2>已知限制与取舍</h2>
 * <p>继承 ApplicationListener</p>
 *
 * @since 2025/10/31
 * @version 1.0
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
