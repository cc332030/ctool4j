package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.core.util.COpt;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

/**
 * <p>
 * Description: COptStrTests
 * </p>
 *
 * @since 2025/12/6
 */
public class COptIterTests {

    @Test
    public void of() {

        Assertions.assertThrowsExactly(NoSuchElementException.class, () -> COpt.of((Iterable<?>) null));

    }

    @Test
    public void empty() {

        Assertions.assertEquals(COpt.emptyIter(), COpt.ofEmptyAble((Iterable<?>) null));

    }

    @Test
    public void isEmpty() {

        Assertions.assertTrue(COpt.ofEmptyAble(CList.of()).isEmpty());
        Assertions.assertFalse(COpt.ofEmptyAble(CList.of(1)).isEmpty());

    }

    @Test
    public void ifNotEmpty() {

        val cCollOpt = COpt.ofEmptyAble(CList.of(1));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () ->
                cCollOpt.ifNotEmpty(list -> {

                    if(1 == list.size()) {
                        throw new IllegalArgumentException();
                    }

                })
        );

        Assertions.assertDoesNotThrow(() -> {
            val cCollOpt2 = COpt.ofEmptyAble(CList.of());
            cCollOpt2.ifNotEmpty((e) -> {
                throw new IllegalArgumentException();
            });
        });

    }

    @Test
    public void ofEmptyAble() {

        Assertions.assertFalse(COpt.ofEmptyAble(CList.of()).isPresent());
        Assertions.assertTrue(COpt.ofEmptyAble(CList.of(1)).isPresent());

    }

    @Test
    public void forEachIfNotEmpty() {

        val cCollOpt = COpt.ofEmptyAble(CList.of(1));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () ->
                cCollOpt.forEachIfNotEmpty((e) -> {
                    if (1 == e) {
                        throw new IllegalArgumentException();
                    }
                })
        );

        Assertions.assertDoesNotThrow(() -> {
            val cCollOpt2 = COpt.ofEmptyAble(CList.of());
            cCollOpt2.forEachIfNotEmpty((e) -> {
                throw new IllegalArgumentException();
            });
        });

    }

}
