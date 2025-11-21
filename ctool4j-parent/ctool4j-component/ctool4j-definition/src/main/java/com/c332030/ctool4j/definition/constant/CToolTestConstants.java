package com.c332030.ctool4j.definition.constant;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * <p>
 * Description: CToolTestConstants
 * </p>
 *
 * @since 2025/11/21
 */
@Slf4j
@UtilityClass
public class CToolTestConstants {

    public static final boolean IS_TEST = ((Supplier<Boolean>) () -> {
        try {

            Class.forName("org.junit.jupiter.api.Test");
            return true;
        } catch (Throwable e) {
            log.debug("check junit result exception", e);
        }
        return false;
    }).get();

}
