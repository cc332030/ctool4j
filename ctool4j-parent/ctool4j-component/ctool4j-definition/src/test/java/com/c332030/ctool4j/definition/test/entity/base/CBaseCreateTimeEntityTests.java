package com.c332030.ctool4j.definition.test.entity.base;

import com.c332030.ctool4j.definition.entity.base.CBaseCreateTimeEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * <p>
 * Description: CBaseCreateTimeEntityTests
 * </p>
 *
 * @since 2026/8/16
 */
public class CBaseCreateTimeEntityTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void noArgsConstructor() {

        CBaseCreateTimeEntity<Long> entity = new CBaseCreateTimeEntity<>();

        Assertions.assertNull(entity.getId());
        Assertions.assertNull(entity.getCreateTime());

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void setterAndGetter() {

        CBaseCreateTimeEntity<Long> entity = new CBaseCreateTimeEntity<>();

        entity.setId(1L);
        Date createTime = new Date();
        entity.setCreateTime(createTime);

        Assertions.assertEquals(Long.valueOf(1L), entity.getId());
        Assertions.assertEquals(createTime, entity.getCreateTime());

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void toStringNullSafe() {

        CBaseCreateTimeEntity<Long> entity = new CBaseCreateTimeEntity<>();

        String str = entity.toString();

        Assertions.assertNotNull(str);
        Assertions.assertTrue(str.contains("CBaseCreateTimeEntity"));
        Assertions.assertTrue(str.contains("id=null"));
        Assertions.assertTrue(str.contains("createTime=null"));

    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void toStringWithValues() {

        CBaseCreateTimeEntity<Long> entity = new CBaseCreateTimeEntity<>();
        entity.setId(1L);
        entity.setCreateTime(new Date(0));

        String str = entity.toString();

        Assertions.assertTrue(str.contains("id=1"));
        Assertions.assertTrue(str.contains("createTime=Thu Jan 01 08:00:00 CST 1970"));

    }

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void equalsAndHashCode() {

        CBaseCreateTimeEntity<Long> a = new CBaseCreateTimeEntity<>();
        a.setId(1L);
        a.setCreateTime(new Date(0));

        CBaseCreateTimeEntity<Long> b = new CBaseCreateTimeEntity<>();
        b.setId(1L);
        b.setCreateTime(new Date(0));

        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());

        CBaseCreateTimeEntity<Long> c = new CBaseCreateTimeEntity<>();
        c.setId(2L);

        Assertions.assertNotEquals(a, c);

    }

    /**
     * 对应测试用例 3.2
     */
    @Test
    public void equalsSameReference() {

        CBaseCreateTimeEntity<Long> a = new CBaseCreateTimeEntity<>();
        Assertions.assertEquals(a, a);

    }

    /**
     * 对应测试用例 4.1
     */
    @Test
    public void builder() {

        CBaseCreateTimeEntity<Long> entity = CBaseCreateTimeEntity.<Long>builder()
            .id(5L)
            .createTime(new Date(0))
            .build();

        Assertions.assertEquals(Long.valueOf(5L), entity.getId());
        Assertions.assertEquals(new Date(0), entity.getCreateTime());

    }

}
