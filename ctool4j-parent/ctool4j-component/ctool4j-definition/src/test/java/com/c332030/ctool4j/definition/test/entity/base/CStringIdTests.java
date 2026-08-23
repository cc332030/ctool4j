package com.c332030.ctool4j.definition.test.entity.base;

import com.c332030.ctool4j.definition.entity.base.CStringId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CStringIdTests
 * </p>
 *
 * @since 2026/8/16
 */
public class CStringIdTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void noArgsConstructor() {

        CStringId id = new CStringId();

        Assertions.assertNull(id.getId());

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void setterAndGetter() {

        CStringId id = new CStringId();
        id.setId("1");

        Assertions.assertEquals("1", id.getId());

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void toStringNullSafe() {

        CStringId id = new CStringId();

        String str = id.toString();

        Assertions.assertNotNull(str);
        Assertions.assertTrue(str.contains("CStringId"));
        Assertions.assertTrue(str.contains("id=null"));

    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void toStringWithValues() {

        CStringId id = new CStringId();
        id.setId("1");

        String str = id.toString();

        Assertions.assertTrue(str.contains("id=1"));

    }

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void equalsAndHashCode() {

        CStringId a = new CStringId();
        a.setId("1");

        CStringId b = new CStringId();
        b.setId("1");

        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());

        CStringId c = new CStringId();
        c.setId("2");

        Assertions.assertNotEquals(a, c);

    }

    /**
     * 对应测试用例 3.2
     */
    @Test
    public void equalsSameReference() {

        CStringId a = new CStringId();
        Assertions.assertEquals(a, a);

    }

    /**
     * 对应测试用例 4.1
     */
    @Test
    public void builder() {

        CStringId id = CStringId.builder()
            .id("5")
            .build();

        Assertions.assertEquals("5", id.getId());

    }

}
