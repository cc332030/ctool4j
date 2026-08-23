package com.c332030.ctool4j.definition.test.entity.base;

import com.c332030.ctool4j.definition.entity.base.CId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CIdTests
 * </p>
 *
 * @since 2026/8/16
 */
public class CIdTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void noArgsConstructor() {

        CId<Long> id = new CId<>();

        Assertions.assertNull(id.getId());

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void setterAndGetter() {

        CId<Long> id = new CId<>();
        id.setId(1L);

        Assertions.assertEquals(Long.valueOf(1L), id.getId());

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void toStringNullSafe() {

        CId<Long> id = new CId<>();

        String str = id.toString();

        Assertions.assertNotNull(str);
        Assertions.assertTrue(str.contains("CId"));
        Assertions.assertTrue(str.contains("id=null"));

    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void toStringWithValues() {

        CId<Long> id = new CId<>();
        id.setId(1L);

        String str = id.toString();

        Assertions.assertTrue(str.contains("id=1"));

    }

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void equalsAndHashCode() {

        CId<Long> a = new CId<>();
        a.setId(1L);

        CId<Long> b = new CId<>();
        b.setId(1L);

        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());

        CId<Long> c = new CId<>();
        c.setId(2L);

        Assertions.assertNotEquals(a, c);

    }

    /**
     * 对应测试用例 3.2
     */
    @Test
    public void equalsSameReference() {

        CId<Long> a = new CId<>();
        Assertions.assertEquals(a, a);

    }

    /**
     * 对应测试用例 4.1
     */
    @Test
    public void builder() {

        CId<Long> id = CId.<Long>builder()
            .id(5L)
            .build();

        Assertions.assertEquals(Long.valueOf(5L), id.getId());

    }

}
