package com.c332030.ctool4j.core.test.function;

import com.c332030.ctool4j.core.function.CBiFunction;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CBiFunctionTest
 * </p>
 *
 * @since 2025/11/6
 */
public class CBiFunctionTest {

    @Test
    public void first() {

        Assertions.assertEquals(1, CBiFunction.FIRST.apply(1, 2));
        Assertions.assertEquals("1", CBiFunction.FIRST.apply("1", "2"));

    }

    @Test
    public void second() {

        Assertions.assertEquals(2, CBiFunction.SECOND.apply(1, 2));
        Assertions.assertEquals("2", CBiFunction.SECOND.apply("1", "2"));

    }

}
