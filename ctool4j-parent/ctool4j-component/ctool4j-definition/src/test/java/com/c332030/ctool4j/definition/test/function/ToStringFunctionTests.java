package com.c332030.ctool4j.definition.test.function;

import com.c332030.ctool4j.definition.function.CFunction;
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

    @Test
    public void applyNormal() {

        ToStringFunction<Integer> function = String::valueOf;

        Assertions.assertEquals("123", function.apply(123));

    }

    @Test
    public void applyNullInput() {

        ToStringFunction<String> function = String::valueOf;

        Assertions.assertEquals("null", function.apply(null));

    }

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

    @Test
    public void convert() {

        ToStringFunction<Integer> cFunction = String::valueOf;

        java.util.function.Function<Integer, String> function = CFunction.convert(cFunction);

        Assertions.assertEquals("456", function.apply(456));

    }

}
