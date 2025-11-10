package com.c332030.ctool4j.core.interfaces;

/**
 * <p>
 * Description: IOperate
 * </p>
 *
 * @since 2025/11/10
 */
public interface IOperate extends IEnumName {

    default String getName() {
        return name();
    }

}
