package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.core.util.COpt;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: COptTests
 * </p>
 *
 * @since 2025/12/6
 */
public class COptTests {

    @Test
    public void of() {

        Assertions.assertThrowsExactly(NullPointerException.class, () -> COpt.of(null));

    }

    @Test
    public void empty() {

        Assertions.assertEquals(COpt.empty(), COpt.ofNullable(null));

    }


    @Test
    public void ofEmptyAble() {

        Assertions.assertFalse(COpt.ofEmptyAble(CList.of()).isPresent());
        Assertions.assertTrue(COpt.ofEmptyAble(CList.of(1)).isPresent());

        Assertions.assertFalse(COpt.ofEmptyAble(CMap.of()).isPresent());
        Assertions.assertTrue(COpt.ofEmptyAble(CMap.of(1, 1)).isPresent());

        Assertions.assertFalse(COpt.ofEmptyAble("").isPresent());
        Assertions.assertTrue(COpt.ofEmptyAble(" ").isPresent());

    }

    @Test
    public void ofBlankAble() {

        Assertions.assertFalse(COpt.ofBlankAble(" ").isPresent());
        Assertions.assertTrue(COpt.ofBlankAble("1").isPresent());

    }

    @Test
    public void isPresent() {

        Assertions.assertTrue(COpt.ofNullable(1).isPresent());
        Assertions.assertFalse(COpt.ofNullable(null).isPresent());

    }

    @Test
    public void get() {

        val result = COpt.of(7)
                .get()
                ;
        Assertions.assertEquals(7, result);

    }

    @Test
    public void orElse() {

        val result = COpt.ofNullable(null)
                .orElse(33)
                ;
        Assertions.assertEquals(33, result);

    }

    @Test
    public void orElseGet() {

        val result = COpt.ofNullable(null)
                .orElseGet(() -> 33)
                ;
        Assertions.assertEquals(33, result);

    }

    @Test
    public void orElseThrow() {

        Assertions.assertThrowsExactly(IllegalArgumentException.class,
                () -> COpt.ofNullable(null)
                        .orElseThrow(IllegalArgumentException::new));

    }

    @Test
    public void map() {

        val result = COpt.of(7)
                .map(String::valueOf)
                .get()
                ;
        Assertions.assertEquals("7", result);

    }

    @Test
    public void flatMap() {

        val result = COpt.of(7)
                .flatMap(e -> COpt.of(String.valueOf(e)))
                .get()
                ;
        Assertions.assertEquals("7", result);

    }

}
