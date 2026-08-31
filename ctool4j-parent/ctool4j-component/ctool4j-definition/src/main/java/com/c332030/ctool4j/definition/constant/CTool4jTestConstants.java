package com.c332030.ctool4j.definition.constant;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * <p>
 * Description: CTool4jTestConstants
 * </p>
 *
 * <p>
 * 本类保留 slf4j 原生 @Slf4j 而非 @CustomLog（原因详见设计文档）
 * </p>
 *
 * @see "doc/design/definition/CTool4jTestConstants.adoc"
 * @since 2025/11/21
 */
@Slf4j
@UtilityClass
public class CTool4jTestConstants {

    /**
     * JUnit 测试类名
     */
    public final String JUNIT_TEST_CLASS_NAME = "org.junit.jupiter.api.Test";

    /**
     * 是否为测试环境（通过能否加载 JUnit 判断）
     */
    public final boolean IS_TEST = ((Supplier<Boolean>) () -> {
        try {

            Class.forName(JUNIT_TEST_CLASS_NAME);
            return true;
        } catch (Throwable e) {
            log.debug("check junit result exception", e);
        }
        return false;
    }).get();

}
