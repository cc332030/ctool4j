package com.c332030.ctool4j.definition.test.entity.base;

import com.c332030.ctool4j.definition.entity.base.CCreateBy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CCreateByTests
 * </p>
 *
 * @since 2026/8/16
 */
public class CCreateByTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void noArgsConstructor() {

        CCreateBy entity = new CCreateBy();

        Assertions.assertNull(entity.getCreateById());
        Assertions.assertNull(entity.getCreateBy());

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void setterAndGetter() {

        CCreateBy entity = new CCreateBy();

        entity.setCreateById(1L);
        entity.setCreateBy("creator");

        Assertions.assertEquals(Long.valueOf(1L), entity.getCreateById());
        Assertions.assertEquals("creator", entity.getCreateBy());

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void toStringNullSafe() {

        CCreateBy entity = new CCreateBy();

        String str = entity.toString();

        Assertions.assertNotNull(str);
        Assertions.assertTrue(str.contains("CCreateBy"));
        Assertions.assertTrue(str.contains("createById=null"));
        Assertions.assertTrue(str.contains("createBy=null"));

    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void toStringWithValues() {

        CCreateBy entity = new CCreateBy();
        entity.setCreateById(1L);
        entity.setCreateBy("creator");

        String str = entity.toString();

        Assertions.assertTrue(str.contains("createById=1"));
        Assertions.assertTrue(str.contains("createBy=creator"));

    }

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void equalsAndHashCode() {

        CCreateBy a = new CCreateBy();
        a.setCreateById(1L);
        a.setCreateBy("creator");

        CCreateBy b = new CCreateBy();
        b.setCreateById(1L);
        b.setCreateBy("creator");

        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());

        CCreateBy c = new CCreateBy();
        c.setCreateById(2L);

        Assertions.assertNotEquals(a, c);

    }

    /**
     * 对应测试用例 3.2
     */
    @Test
    public void equalsSameReference() {

        CCreateBy a = new CCreateBy();
        Assertions.assertEquals(a, a);

    }

    /**
     * 对应测试用例 4.1
     */
    @Test
    public void builder() {

        CCreateBy entity = CCreateBy.builder()
            .createById(5L)
            .createBy("creator")
            .build();

        Assertions.assertEquals(Long.valueOf(5L), entity.getCreateById());
        Assertions.assertEquals("creator", entity.getCreateBy());

    }

}
