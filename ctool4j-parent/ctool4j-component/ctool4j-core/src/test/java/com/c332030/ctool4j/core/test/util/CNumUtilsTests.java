package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CNumUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CNumUtilsTests
 * </p>
 *
 * @since 2025/12/22
 */
public class CNumUtilsTests {

    @Test
    public void to62() {

        Assertions.assertEquals("1", CNumUtils.to62(1));
        Assertions.assertEquals("10", CNumUtils.to62(62));
        Assertions.assertEquals("100", CNumUtils.to62(3844));

        Assertions.assertEquals("23", CNumUtils.to62(Byte.MAX_VALUE));
        Assertions.assertEquals("8wv", CNumUtils.to62(Short.MAX_VALUE));
        Assertions.assertEquals("2lkCB1", CNumUtils.to62(Integer.MAX_VALUE));
        Assertions.assertEquals("aZl8N0y58M7", CNumUtils.to62(Long.MAX_VALUE));

    }

    @Test
    public void max() {

        Assertions.assertEquals(9, CNumUtils.max(
            -1, 1, 3, 9
        ));

        Assertions.assertEquals(9, CNumUtils.max(
            1, 3, 9
        ));

        Assertions.assertEquals(-1, CNumUtils.max(
            -1, -3, -9
        ));

    }

    @Test
    public void min() {

        Assertions.assertEquals(-1, CNumUtils.min(
            -1, 1, 3, 9
        ));

        Assertions.assertEquals(1, CNumUtils.min(
            1, 3, 9
        ));

        Assertions.assertEquals(-9, CNumUtils.min(
            -1, -3, -9
        ));

    }

}
