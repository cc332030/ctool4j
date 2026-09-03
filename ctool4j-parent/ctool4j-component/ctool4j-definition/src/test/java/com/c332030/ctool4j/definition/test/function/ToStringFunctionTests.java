package com.c332030.ctool4j.definition.test.function;

import com.c332030.ctool4j.definition.function.ToStringFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * <p>
 * Description: ToStringFunctionTests
 * </p>
 *
 * @since 2026/8/14
 */
public class ToStringFunctionTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void applyNormal() {

        ToStringFunction<Integer> function = String::valueOf;

        Assertions.assertEquals("123", function.apply(123));

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void applyNullInput() {

        ToStringFunction<String> function = String::valueOf;

        Assertions.assertEquals("null", function.apply(null));

    }

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void applySneakyThrowsCheckedException() {

        ToStringFunction<Integer> function = i -> {
            throw new IOException("io error");
        };

        Assertions.assertThrowsExactly(
            IOException.class,
            () -> function.apply(1)
        );

    }

}
