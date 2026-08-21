package com.c332030.ctool4j.definition.test.entity.base;

import com.c332030.ctool4j.definition.entity.base.CBaseTimeEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * <p>
 * Description: CBaseTimeEntityTests
 * </p>
 *
 * @since 2026/8/16
 */
public class CBaseTimeEntityTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void noArgsConstructor() {

        CBaseTimeEntity<Long> entity = new CBaseTimeEntity<>();

        Assertions.assertNull(entity.getId());
        Assertions.assertNull(entity.getCreateTime());
        Assertions.assertNull(entity.getUpdateTime());

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void setterAndGetter() {

        CBaseTimeEntity<Long> entity = new CBaseTimeEntity<>();

        entity.setId(1L);
        Date createTime = new Date();
        Date updateTime = new Date();
        entity.setCreateTime(createTime);
        entity.setUpdateTime(updateTime);

        Assertions.assertEquals(Long.valueOf(1L), entity.getId());
        Assertions.assertEquals(createTime, entity.getCreateTime());
        Assertions.assertEquals(updateTime, entity.getUpdateTime());

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void toStringNullSafe() {

        CBaseTimeEntity<Long> entity = new CBaseTimeEntity<>();

        String str = entity.toString();

        Assertions.assertNotNull(str);
        Assertions.assertTrue(str.contains("CBaseTimeEntity"));
        Assertions.assertTrue(str.contains("id=null"));
        Assertions.assertTrue(str.contains("createTime=null"));
        Assertions.assertTrue(str.contains("updateTime=null"));

    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void toStringWithValues() {

        CBaseTimeEntity<Long> entity = new CBaseTimeEntity<>();
        entity.setId(1L);
        entity.setCreateTime(new Date(0));
        entity.setUpdateTime(new Date(0));

        String str = entity.toString();

        Assertions.assertTrue(str.contains("id=1"));
        Assertions.assertTrue(str.contains("createTime=Thu Jan 01 08:00:00 CST 1970"));
        Assertions.assertTrue(str.contains("updateTime=Thu Jan 01 08:00:00 CST 1970"));

    }

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void equalsAndHashCode() {

        CBaseTimeEntity<Long> a = new CBaseTimeEntity<>();
        a.setId(1L);
        a.setCreateTime(new Date(0));
        a.setUpdateTime(new Date(0));

        CBaseTimeEntity<Long> b = new CBaseTimeEntity<>();
        b.setId(1L);
        b.setCreateTime(new Date(0));
        b.setUpdateTime(new Date(0));

        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());

        CBaseTimeEntity<Long> c = new CBaseTimeEntity<>();
        c.setId(2L);

        Assertions.assertNotEquals(a, c);

    }

    /**
     * 对应测试用例 3.2
     */
    @Test
    public void equalsSameReference() {

        CBaseTimeEntity<Long> a = new CBaseTimeEntity<>();
        Assertions.assertEquals(a, a);

    }

    /**
     * 对应测试用例 4.1
     */
    @Test
    public void builder() {

        CBaseTimeEntity<Long> entity = CBaseTimeEntity.<Long>builder()
            .id(5L)
            .createTime(new Date(0))
            .updateTime(new Date(0))
            .build();

        Assertions.assertEquals(Long.valueOf(5L), entity.getId());
        Assertions.assertEquals(new Date(0), entity.getCreateTime());
        Assertions.assertEquals(new Date(0), entity.getUpdateTime());

    }

}
