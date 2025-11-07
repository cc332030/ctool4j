package com.c332030.ctool4j.core.test.function;

import com.c332030.ctool4j.core.function.CTriFunction;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CTriFunctionTest
 * </p>
 *
 * @since 2025/11/6
 */
public class CTriFunctionTest {

    @Test
    public void first() {

        Assertions.assertEquals(1, CTriFunction.FIRST.apply(1, 2, 3));
        Assertions.assertEquals("1", CTriFunction.FIRST.apply("1", "2", "3"));

    }

    @Test
    public void second() {

        Assertions.assertEquals(2, CTriFunction.SECOND.apply(1, 2, 3));
        Assertions.assertEquals("2", CTriFunction.SECOND.apply("1", "2", "3"));

    }

    @Test
    public void third() {

        Assertions.assertEquals(3, CTriFunction.THIRD.apply(1, 2, 3));
        Assertions.assertEquals("3", CTriFunction.THIRD.apply("1", "2", "3"));

    }

}
