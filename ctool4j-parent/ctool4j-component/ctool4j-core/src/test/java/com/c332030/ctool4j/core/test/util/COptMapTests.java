package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.core.util.COpt;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * <p>
 * Description: COptStrTests
 * </p>
 *
 * @since 2025/12/6
 */
public class COptMapTests {

    @Test
    public void of() {

        Assertions.assertThrowsExactly(NoSuchElementException.class, () -> COpt.of((Map<?, ?>) null));

    }

    @Test
    public void empty() {

        Assertions.assertEquals(COpt.emptyMap(), COpt.ofEmptyAble((Map<?, ?>) null));

    }

    @Test
    public void isEmpty() {

        Assertions.assertTrue(COpt.ofEmptyAble(CMap.of()).isEmpty());
        Assertions.assertFalse(COpt.ofEmptyAble(CMap.of(1, 1)).isEmpty());

    }

    @Test
    public void ifNotEmpty() {

        val cCollOpt = COpt.ofEmptyAble(CMap.of(1, 1));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () ->
                cCollOpt.ifNotEmpty(value -> {

                    if(1 == value.size()) {
                        throw new IllegalArgumentException();
                    }

                })
        );

        Assertions.assertDoesNotThrow(() -> {
            val cCollOpt2 = COpt.ofEmptyAble(CMap.of());
            cCollOpt2.ifNotEmpty((e) -> {
                throw new IllegalArgumentException();
            });
        });

    }

    @Test
    public void ofEmptyAble() {

        Assertions.assertFalse(COpt.ofEmptyAble(CMap.of()).isPresent());
        Assertions.assertTrue(COpt.ofEmptyAble(CMap.of(1, 1)).isPresent());

    }

    @Test
    public void forEachIfNotEmpty() {

        val cCollOpt = COpt.ofEmptyAble(CMap.of(1, 1));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () ->
                cCollOpt.forEachIfNotEmpty((k, v) -> {
                    if (1 == k && 1 == v) {
                        throw new IllegalArgumentException();
                    }
                })
        );

        Assertions.assertDoesNotThrow(() -> {
            val cCollOpt2 = COpt.ofEmptyAble(CMap.of());
            cCollOpt2.forEachIfNotEmpty((k, v) -> {
                throw new IllegalArgumentException();
            });
        });

    }

}
