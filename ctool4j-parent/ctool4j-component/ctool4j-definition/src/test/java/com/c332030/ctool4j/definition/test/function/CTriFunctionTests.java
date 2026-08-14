package com.c332030.ctool4j.definition.test.function;

import com.c332030.ctool4j.definition.function.CTriFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * <p>
 * Description: CTriFunctionTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CTriFunctionTests {

    @Test
    public void applyNormal() {

        CTriFunction<Integer, Integer, Integer, Integer> function = (a, b, c) -> a + b + c;

        Assertions.assertEquals(Integer.valueOf(6), function.apply(1, 2, 3));

    }

    @Test
    public void applyNullInputs() {

        CTriFunction<String, String, String, String> function = (a, b, c) -> a + b + c;

        Assertions.assertEquals("nullnullnull", function.apply(null, null, null));

    }

    @Test
    public void applySneakyThrowsCheckedException() {

        CTriFunction<String, String, String, String> function = (a, b, c) -> {
            throw new IOException("io error");
        };

        Assertions.assertThrowsExactly(
            IOException.class,
            () -> function.apply("a", "b", "c")
        );

    }

    @Test
    public void first() {

        CTriFunction<Integer, Integer, Integer, Integer> first = CTriFunction.first();

        Assertions.assertEquals(Integer.valueOf(1), first.apply(1, 2, 3));

    }

    @Test
    public void second() {

        CTriFunction<Integer, Integer, Integer, Integer> second = CTriFunction.second();

        Assertions.assertEquals(Integer.valueOf(2), second.apply(1, 2, 3));

    }

    @Test
    public void third() {

        CTriFunction<Integer, Integer, Integer, Integer> third = CTriFunction.third();

        Assertions.assertEquals(Integer.valueOf(3), third.apply(1, 2, 3));

    }

    @Test
    public void staticApplyNullFunction() {

        Assertions.assertNull(CTriFunction.apply(null, 1, 2, 3));

    }

    @Test
    public void staticApplyNormal() {

        CTriFunction<Integer, Integer, Integer, Integer> function = (a, b, c) -> a + b + c;

        Assertions.assertEquals(Integer.valueOf(6), CTriFunction.apply(function, 1, 2, 3));

    }

    @Test
    public void convert() {

        CTriFunction<Integer, Integer, Integer, Integer> cFunction = (a, b, c) -> a + b + c;

        CTriFunction<Integer, Integer, Integer, Integer> function = CTriFunction.convert(cFunction);

        Assertions.assertEquals(Integer.valueOf(9), function.apply(2, 3, 4));

    }

    @Test
    public void convertNullCTriFunction() {

        CTriFunction<Integer, Integer, Integer, Integer> function = CTriFunction.convert(null);

        Assertions.assertNull(function.apply(2, 3, 4));

    }

}
