package com.c332030.ctool4j.definition.test.entity.base;

import com.c332030.ctool4j.definition.entity.base.CLongId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CLongIdTests
 * </p>
 *
 * @since 2026/8/16
 */
public class CLongIdTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void noArgsConstructor() {

        CLongId id = new CLongId();

        Assertions.assertNull(id.getId());

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void setterAndGetter() {

        CLongId id = new CLongId();
        id.setId(1L);

        Assertions.assertEquals(Long.valueOf(1L), id.getId());

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void toStringNullSafe() {

        CLongId id = new CLongId();

        String str = id.toString();

        Assertions.assertNotNull(str);
        Assertions.assertTrue(str.contains("CLongId"));
        Assertions.assertTrue(str.contains("id=null"));

    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void toStringWithValues() {

        CLongId id = new CLongId();
        id.setId(1L);

        String str = id.toString();

        Assertions.assertTrue(str.contains("id=1"));

    }

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void equalsAndHashCode() {

        CLongId a = new CLongId();
        a.setId(1L);

        CLongId b = new CLongId();
        b.setId(1L);

        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());

        CLongId c = new CLongId();
        c.setId(2L);

        Assertions.assertNotEquals(a, c);

    }

    /**
     * 对应测试用例 3.2
     */
    @Test
    public void equalsSameReference() {

        CLongId a = new CLongId();
        Assertions.assertEquals(a, a);

    }

    /**
     * 对应测试用例 4.1
     */
    @Test
    public void builder() {

        CLongId id = CLongId.builder()
            .id(5L)
            .build();

        Assertions.assertEquals(Long.valueOf(5L), id.getId());

    }

}
