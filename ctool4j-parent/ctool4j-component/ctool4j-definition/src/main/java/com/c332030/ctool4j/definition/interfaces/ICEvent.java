package com.c332030.ctool4j.definition.interfaces;

/**
 * <p>
 * Description: ICEvent
 * </p>
 *
 * @since 2025/11/27
 */
public interface ICEvent<E> {

    void onEvent(E event);

}
