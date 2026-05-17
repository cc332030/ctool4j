package com.c332030.ctool4j.definition.interfaces;

/**
 * <p>
 * Description: ICOperate
 * </p>
 *
 * @since 2025/11/10
 */
public interface ICOperate extends ICEnumName {

    default String getName() {
        return name();
    }

}
