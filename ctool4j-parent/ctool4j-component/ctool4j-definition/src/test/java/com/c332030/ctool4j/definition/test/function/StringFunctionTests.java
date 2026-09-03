package com.c332030.ctool4j.definition.test.function;

import com.c332030.ctool4j.definition.function.StringFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * <p>
 * Description: StringFunctionTests
 * </p>
 *
 * @since 2026/8/14
 */
public class StringFunctionTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void applyNormal() {

        StringFunction<Integer> function = Integer::valueOf;

        Assertions.assertEquals(Integer.valueOf(123), function.apply("123"));

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void applyNullInput() {

        StringFunction<String> function = s -> s + "x";

        Assertions.assertEquals("nullx", function.apply(null));

    }

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void applySneakyThrowsCheckedException() {

        StringFunction<String> function = s -> {
            throw new IOException("io error");
        };

        Assertions.assertThrowsExactly(
            IOException.class,
            () -> function.apply("input")
        );

    }

}
