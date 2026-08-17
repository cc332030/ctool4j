package com.c332030.ctool4j.definition.interfaces;

/**
 * <p>
 * Description: ICEvent
 * </p>
 *
 * @since 2025/11/27
 */
public interface ICEvent<E> {

    /**
     * 处理事件
     * @param event 事件
     */
    void onEvent(E event);

}
