package com.c332030.ctool4j.definition.test.entity.base;

import com.c332030.ctool4j.definition.entity.base.CCreateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * <p>
 * Description: CCreateTimeTests
 * </p>
 *
 * @since 2026/8/16
 */
public class CCreateTimeTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void noArgsConstructor() {

        CCreateTime entity = new CCreateTime();

        Assertions.assertNull(entity.getCreateTime());

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void setterAndGetter() {

        CCreateTime entity = new CCreateTime();

        Date createTime = new Date();
        entity.setCreateTime(createTime);

        Assertions.assertEquals(createTime, entity.getCreateTime());

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void toStringNullSafe() {

        CCreateTime entity = new CCreateTime();

        String str = entity.toString();

        Assertions.assertNotNull(str);
        Assertions.assertTrue(str.contains("CCreateTime"));
        Assertions.assertTrue(str.contains("createTime=null"));

    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void toStringWithValues() {

        CCreateTime entity = new CCreateTime();
        entity.setCreateTime(new Date(0));

        String str = entity.toString();

        Assertions.assertTrue(str.contains("createTime=Thu Jan 01 08:00:00 CST 1970"));

    }

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void equalsAndHashCode() {

        CCreateTime a = new CCreateTime();
        a.setCreateTime(new Date(0));

        CCreateTime b = new CCreateTime();
        b.setCreateTime(new Date(0));

        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());

        CCreateTime c = new CCreateTime();
        c.setCreateTime(new Date(1));

        Assertions.assertNotEquals(a, c);

    }

    /**
     * 对应测试用例 3.2
     */
    @Test
    public void equalsSameReference() {

        CCreateTime a = new CCreateTime();
        Assertions.assertEquals(a, a);

    }

    /**
     * 对应测试用例 4.1
     */
    @Test
    public void builder() {

        CCreateTime entity = CCreateTime.builder()
            .createTime(new Date(0))
            .build();

        Assertions.assertEquals(new Date(0), entity.getCreateTime());

    }

}
