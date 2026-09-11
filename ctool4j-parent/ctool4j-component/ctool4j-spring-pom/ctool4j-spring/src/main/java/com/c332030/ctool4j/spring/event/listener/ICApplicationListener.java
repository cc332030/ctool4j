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
 * <h2>能力目录</h2>
 * <p>{@code ICApplicationListener}：应用事件监听基接口。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>继承 ApplicationListener</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>事件监听</p>
 * <h2>不适用与边界场景</h2>
 * <p>泛型事件</p>
 * <h2>已知限制与取舍</h2>
 * <p>泛型事件</p>
 *
 * @since 2025/10/31
 * @version 1.0
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
