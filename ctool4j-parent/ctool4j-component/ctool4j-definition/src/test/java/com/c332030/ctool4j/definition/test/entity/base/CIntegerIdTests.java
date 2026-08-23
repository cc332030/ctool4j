package com.c332030.ctool4j.definition.test.entity.base;

import com.c332030.ctool4j.definition.entity.base.CIntegerId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CIntegerIdTests
 * </p>
 *
 * @since 2026/8/16
 */
public class CIntegerIdTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void noArgsConstructor() {

        CIntegerId id = new CIntegerId();

        Assertions.assertNull(id.getId());

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void setterAndGetter() {

        CIntegerId id = new CIntegerId();
        id.setId(1);

        Assertions.assertEquals(Integer.valueOf(1), id.getId());

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void toStringNullSafe() {

        CIntegerId id = new CIntegerId();

        String str = id.toString();

        Assertions.assertNotNull(str);
        Assertions.assertTrue(str.contains("CIntegerId"));
        Assertions.assertTrue(str.contains("id=null"));

    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void toStringWithValues() {

        CIntegerId id = new CIntegerId();
        id.setId(1);

        String str = id.toString();

        Assertions.assertTrue(str.contains("id=1"));

    }

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void equalsAndHashCode() {

        CIntegerId a = new CIntegerId();
        a.setId(1);

        CIntegerId b = new CIntegerId();
        b.setId(1);

        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());

        CIntegerId c = new CIntegerId();
        c.setId(2);

        Assertions.assertNotEquals(a, c);

    }

    /**
     * 对应测试用例 3.2
     */
    @Test
    public void equalsSameReference() {

        CIntegerId a = new CIntegerId();
        Assertions.assertEquals(a, a);

    }

    /**
     * 对应测试用例 4.1
     */
    @Test
    public void builder() {

        CIntegerId id = CIntegerId.builder()
            .id(5)
            .build();

        Assertions.assertEquals(Integer.valueOf(5), id.getId());

    }

}
