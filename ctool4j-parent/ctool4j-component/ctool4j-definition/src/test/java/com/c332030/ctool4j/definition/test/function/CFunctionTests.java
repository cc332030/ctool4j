package com.c332030.ctool4j.definition.test.function;

import com.c332030.ctool4j.definition.function.CFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.Function;

/**
 * <p>
 * Description: CFunctionTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CFunctionTests {

    @Test
    public void applyNormal() {

        CFunction<String, Integer> function = Integer::valueOf;

        Assertions.assertEquals(Integer.valueOf(123), function.apply("123"));

    }

    @Test
    public void applyNullInput() {

        CFunction<String, String> function = s -> "x";

        Assertions.assertEquals("x", function.apply(null));

    }

    @Test
    public void applySneakyThrowsCheckedException() {

        CFunction<String, String> function = s -> {
            throw new IOException("io error");
        };

        Assertions.assertThrowsExactly(
            IOException.class,
            () -> function.apply("input")
        );

    }

    @Test
    public void applySneakyThrowsRuntimeException() {

        CFunction<String, String> function = s -> {
            throw new IllegalArgumentException("bad arg");
        };

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> function.apply("input")
        );

    }

    @Test
    public void self() {

        CFunction<String, String> self = CFunction.self();

        Assertions.assertEquals("abc", self.apply("abc"));
        Assertions.assertNull(self.apply(null));

    }

    @Test
    public void empty() {

        CFunction<String, String> empty = CFunction.empty();

        Assertions.assertNull(empty.apply("abc"));

    }

    @Test
    public void staticApplyNullFunction() {

        Assertions.assertNull(CFunction.apply(null, "input"));

    }

    @Test
    public void staticApplyNullFunctionWithNullInput() {

        Assertions.assertNull(CFunction.apply(null, null));

    }

    @Test
    public void staticApplyNormal() {

        Function<String, Integer> function = Integer::valueOf;

        Assertions.assertEquals(Integer.valueOf(123), CFunction.apply(function, "123"));

    }

    @Test
    public void convert() {

        CFunction<String, Integer> cFunction = Integer::valueOf;

        Function<String, Integer> function = CFunction.convert(cFunction);

        Assertions.assertEquals(Integer.valueOf(456), function.apply("456"));

    }

    @Test
    public void convertNullCFunction() {

        Function<String, Integer> function = CFunction.convert(null);

        Assertions.assertNull(function.apply("456"));

    }

}
