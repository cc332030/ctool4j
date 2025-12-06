package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.COpt;
import com.c332030.ctool4j.core.util.CStrOpt;
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
public class COptStrTests {

    @Test
    public void of() {

        Assertions.assertThrowsExactly(NoSuchElementException.class, () -> COpt.of((String)null));

    }

    @Test
    public void empty() {

        Assertions.assertEquals(CStrOpt.emptyStr(), COpt.ofEmptyAble((String)null));
        Assertions.assertEquals(CStrOpt.emptyStr(), COpt.ofBlankAble((String)null));

    }

    @Test
    public void isEmpty() {

        Assertions.assertTrue(COpt.ofEmptyAble("").isEmpty());
        Assertions.assertFalse(COpt.ofEmptyAble(" ").isEmpty());

    }

    @Test
    public void ofEmptyAble() {

        Assertions.assertFalse(COpt.ofEmptyAble("").isPresent());
        Assertions.assertTrue(COpt.ofEmptyAble(" ").isPresent());

    }

    @Test
    public void isBlank() {

        Assertions.assertTrue(COpt.ofEmptyAble(" ").isBlank());
        Assertions.assertFalse(COpt.ofEmptyAble("7").isBlank());

    }

    @Test
    public void ofBlankAble() {

        Assertions.assertFalse(COpt.ofBlankAble(" ").isPresent());
        Assertions.assertTrue(COpt.ofBlankAble("1").isPresent());

    }

}
