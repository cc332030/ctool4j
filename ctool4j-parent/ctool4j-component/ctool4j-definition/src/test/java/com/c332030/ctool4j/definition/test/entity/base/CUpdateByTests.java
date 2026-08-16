package com.c332030.ctool4j.definition.test.entity.base;

import com.c332030.ctool4j.definition.entity.base.CUpdateBy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CUpdateByTests
 * </p>
 *
 * @since 2026/8/16
 */
public class CUpdateByTests {

    @Test
    public void noArgsConstructor() {

        CUpdateBy entity = new CUpdateBy();

        Assertions.assertNull(entity.getCreateById());
        Assertions.assertNull(entity.getCreateBy());
        Assertions.assertNull(entity.getUpdateById());
        Assertions.assertNull(entity.getUpdateBy());

    }

    @Test
    public void setterAndGetter() {

        CUpdateBy entity = new CUpdateBy();

        entity.setCreateById(1L);
        entity.setCreateBy("creator");
        entity.setUpdateById(2L);
        entity.setUpdateBy("updater");

        Assertions.assertEquals(Long.valueOf(1L), entity.getCreateById());
        Assertions.assertEquals("creator", entity.getCreateBy());
        Assertions.assertEquals(Long.valueOf(2L), entity.getUpdateById());
        Assertions.assertEquals("updater", entity.getUpdateBy());

    }

    @Test
    public void toStringNullSafe() {

        CUpdateBy entity = new CUpdateBy();

        String str = entity.toString();

        Assertions.assertNotNull(str);
        Assertions.assertTrue(str.contains("CUpdateBy"));
        Assertions.assertTrue(str.contains("createById=null"));
        Assertions.assertTrue(str.contains("createBy=null"));
        Assertions.assertTrue(str.contains("updateById=null"));
        Assertions.assertTrue(str.contains("updateBy=null"));

    }

    @Test
    public void toStringWithValues() {

        CUpdateBy entity = new CUpdateBy();
        entity.setCreateById(1L);
        entity.setCreateBy("creator");
        entity.setUpdateById(2L);
        entity.setUpdateBy("updater");

        String str = entity.toString();

        Assertions.assertTrue(str.contains("createById=1"));
        Assertions.assertTrue(str.contains("createBy=creator"));
        Assertions.assertTrue(str.contains("updateById=2"));
        Assertions.assertTrue(str.contains("updateBy=updater"));

    }

    @Test
    public void equalsAndHashCode() {

        CUpdateBy a = new CUpdateBy();
        a.setCreateById(1L);
        a.setCreateBy("creator");
        a.setUpdateById(2L);
        a.setUpdateBy("updater");

        CUpdateBy b = new CUpdateBy();
        b.setCreateById(1L);
        b.setCreateBy("creator");
        b.setUpdateById(2L);
        b.setUpdateBy("updater");

        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());

        CUpdateBy c = new CUpdateBy();
        c.setCreateById(2L);

        Assertions.assertNotEquals(a, c);

    }

    @Test
    public void equalsSameReference() {

        CUpdateBy a = new CUpdateBy();
        Assertions.assertEquals(a, a);

    }

    @Test
    public void builder() {

        CUpdateBy entity = CUpdateBy.builder()
            .createById(1L)
            .createBy("creator")
            .updateById(2L)
            .updateBy("updater")
            .build();

        Assertions.assertEquals(Long.valueOf(1L), entity.getCreateById());
        Assertions.assertEquals("creator", entity.getCreateBy());
        Assertions.assertEquals(Long.valueOf(2L), entity.getUpdateById());
        Assertions.assertEquals("updater", entity.getUpdateBy());

    }

}
