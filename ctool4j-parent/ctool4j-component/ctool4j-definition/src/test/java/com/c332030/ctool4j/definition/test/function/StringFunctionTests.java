package com.c332030.ctool4j.definition.test.function;

import com.c332030.ctool4j.definition.function.CFunction;
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

    @Test
    public void applyNormal() {

        StringFunction<Integer> function = Integer::valueOf;

        Assertions.assertEquals(Integer.valueOf(123), function.apply("123"));

    }

    @Test
    public void applyNullInput() {

        StringFunction<String> function = s -> s + "x";

        Assertions.assertEquals("nullx", function.apply(null));

    }

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

    @Test
    public void convert() {

        StringFunction<Integer> cFunction = Integer::valueOf;

        java.util.function.Function<String, Integer> function = CFunction.convert(cFunction);

        Assertions.assertEquals(Integer.valueOf(789), function.apply("789"));

    }

}
