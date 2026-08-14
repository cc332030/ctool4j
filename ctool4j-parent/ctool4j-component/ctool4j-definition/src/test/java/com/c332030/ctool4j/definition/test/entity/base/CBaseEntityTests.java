package com.c332030.ctool4j.definition.test.entity.base;

import com.c332030.ctool4j.definition.entity.base.CBaseEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * <p>
 * Description: CBaseEntityTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CBaseEntityTests {

    @Test
    public void noArgsConstructor() {

        CBaseEntity<Long> entity = new CBaseEntity<>();

        Assertions.assertNull(entity.getId());
        Assertions.assertNull(entity.getCreateTime());
        Assertions.assertNull(entity.getUpdateTime());
        Assertions.assertNull(entity.getCreateById());
        Assertions.assertNull(entity.getCreateBy());
        Assertions.assertNull(entity.getUpdateById());
        Assertions.assertNull(entity.getUpdateBy());

    }

    @Test
    public void setterAndGetter() {

        CBaseEntity<Long> entity = new CBaseEntity<>();

        Long id = 1L;
        Date createTime = new Date();
        entity.setId(id);
        entity.setCreateTime(createTime);
        entity.setCreateById(100L);
        entity.setCreateBy("creator");

        Assertions.assertEquals(id, entity.getId());
        Assertions.assertEquals(createTime, entity.getCreateTime());
        Assertions.assertEquals(Long.valueOf(100L), entity.getCreateById());
        Assertions.assertEquals("creator", entity.getCreateBy());

    }

    @Test
    public void toStringNullSafe() {

        CBaseEntity<Long> entity = new CBaseEntity<>();

        String str = entity.toString();

        Assertions.assertNotNull(str);
        Assertions.assertTrue(str.contains("CBaseEntity"));
        Assertions.assertTrue(str.contains("createById=null"));

    }

    @Test
    public void toStringWithValues() {

        CBaseEntity<Long> entity = new CBaseEntity<>();
        entity.setId(1L);
        entity.setCreateBy("admin");

        String str = entity.toString();

        Assertions.assertTrue(str.contains("id=1"));
        Assertions.assertTrue(str.contains("createBy=admin"));

    }

    @Test
    public void equalsAndHashCode() {

        CBaseEntity<Long> a = new CBaseEntity<>();
        a.setId(1L);

        CBaseEntity<Long> b = new CBaseEntity<>();
        b.setId(1L);

        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());

        CBaseEntity<Long> c = new CBaseEntity<>();
        c.setId(2L);

        Assertions.assertNotEquals(a, c);

    }

    @Test
    public void equalsSameReference() {

        CBaseEntity<Long> a = new CBaseEntity<>();
        Assertions.assertEquals(a, a);

    }

    @Test
    public void builder() {

        CBaseEntity<Long> entity = CBaseEntity.<Long>builder()
            .id(5L)
            .createBy("creator")
            .build();

        Assertions.assertEquals(Long.valueOf(5L), entity.getId());
        Assertions.assertEquals("creator", entity.getCreateBy());

    }

}
