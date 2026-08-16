package com.c332030.ctool4j.definition.test.entity.base;

import com.c332030.ctool4j.definition.entity.base.CUpdateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * <p>
 * Description: CUpdateTimeTests
 * </p>
 *
 * @since 2026/8/16
 */
public class CUpdateTimeTests {

    @Test
    public void noArgsConstructor() {

        CUpdateTime entity = new CUpdateTime();

        Assertions.assertNull(entity.getCreateTime());
        Assertions.assertNull(entity.getUpdateTime());

    }

    @Test
    public void setterAndGetter() {

        CUpdateTime entity = new CUpdateTime();

        Date createTime = new Date();
        Date updateTime = new Date();
        entity.setCreateTime(createTime);
        entity.setUpdateTime(updateTime);

        Assertions.assertEquals(createTime, entity.getCreateTime());
        Assertions.assertEquals(updateTime, entity.getUpdateTime());

    }

    @Test
    public void toStringNullSafe() {

        CUpdateTime entity = new CUpdateTime();

        String str = entity.toString();

        Assertions.assertNotNull(str);
        Assertions.assertTrue(str.contains("CUpdateTime"));
        Assertions.assertTrue(str.contains("createTime=null"));
        Assertions.assertTrue(str.contains("updateTime=null"));

    }

    @Test
    public void toStringWithValues() {

        CUpdateTime entity = new CUpdateTime();
        entity.setCreateTime(new Date(0));
        entity.setUpdateTime(new Date(0));

        String str = entity.toString();

        Assertions.assertTrue(str.contains("createTime=Thu Jan 01 08:00:00 CST 1970"));
        Assertions.assertTrue(str.contains("updateTime=Thu Jan 01 08:00:00 CST 1970"));

    }

    @Test
    public void equalsAndHashCode() {

        CUpdateTime a = new CUpdateTime();
        a.setCreateTime(new Date(0));
        a.setUpdateTime(new Date(0));

        CUpdateTime b = new CUpdateTime();
        b.setCreateTime(new Date(0));
        b.setUpdateTime(new Date(0));

        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());

        CUpdateTime c = new CUpdateTime();
        c.setCreateTime(new Date(1));

        Assertions.assertNotEquals(a, c);

    }

    @Test
    public void equalsSameReference() {

        CUpdateTime a = new CUpdateTime();
        Assertions.assertEquals(a, a);

    }

    @Test
    public void builder() {

        CUpdateTime entity = CUpdateTime.builder()
            .createTime(new Date(0))
            .updateTime(new Date(0))
            .build();

        Assertions.assertEquals(new Date(0), entity.getCreateTime());
        Assertions.assertEquals(new Date(0), entity.getUpdateTime());

    }

}
