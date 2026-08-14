package com.c332030.ctool4j.definition.test.function;

import com.c332030.ctool4j.definition.function.CBiFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.BiFunction;

/**
 * <p>
 * Description: CBiFunctionTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CBiFunctionTests {

    @Test
    public void applyNormal() {

        CBiFunction<Integer, Integer, Integer> function = Integer::sum;

        Assertions.assertEquals(Integer.valueOf(3), function.apply(1, 2));

    }

    @Test
    public void applyNullInputs() {

        CBiFunction<String, String, String> function = (a, b) -> a + b;

        Assertions.assertEquals("nullnull", function.apply(null, null));

    }

    @Test
    public void applySneakyThrowsCheckedException() {

        CBiFunction<String, String, String> function = (a, b) -> {
            throw new IOException("io error");
        };

        Assertions.assertThrowsExactly(
            IOException.class,
            () -> function.apply("a", "b")
        );

    }

    @Test
    public void first() {

        BiFunction<Integer, Integer, Integer> first = CBiFunction.first();

        Assertions.assertEquals(Integer.valueOf(1), first.apply(1, 2));

    }

    @Test
    public void second() {

        BiFunction<Integer, Integer, Integer> second = CBiFunction.second();

        Assertions.assertEquals(Integer.valueOf(2), second.apply(1, 2));

    }

    @Test
    public void staticApplyNullFunction() {

        Assertions.assertNull(CBiFunction.apply(null, 1, 2));

    }

    @Test
    public void staticApplyNormal() {

        BiFunction<Integer, Integer, Integer> function = Integer::sum;

        Assertions.assertEquals(Integer.valueOf(3), CBiFunction.apply(function, 1, 2));

    }

    @Test
    public void convert() {

        CBiFunction<Integer, Integer, Integer> cFunction = Integer::sum;

        BiFunction<Integer, Integer, Integer> function = CBiFunction.convert(cFunction);

        Assertions.assertEquals(Integer.valueOf(5), function.apply(2, 3));

    }

    @Test
    public void convertNullCBiFunction() {

        BiFunction<Integer, Integer, Integer> function = CBiFunction.convert(null);

        Assertions.assertNull(function.apply(2, 3));

    }

}
