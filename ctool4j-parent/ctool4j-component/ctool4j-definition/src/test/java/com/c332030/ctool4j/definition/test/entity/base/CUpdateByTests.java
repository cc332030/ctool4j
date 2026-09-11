package com.c332030.ctool4j.definition.test.entity.base;

import com.c332030.ctool4j.definition.entity.base.CUpdateBy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CUpdateByTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「构造 / 设置取值 / toString / equals/hashCode / builder」多个维度组织，验证 Lombok @Data/@SuperBuilder 行为。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 @Data/@SuperBuilder 生成方法的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：无参构造；setter/getter；toString null 安全与有值；equals/hashCode；同引用相等；builder。</li>
 *   <li>未覆盖：无。</li>
 * </ul>
 * <h2>构造与取值</h2>
 * <ul>
 *   <li>1.1 无参构造：字段为 null（noArgsConstructor）</li>
 *   <li>1.2 setter/getter：设置与读取（setterAndGetter）</li>
 * </ul>
 * <h2>toString</h2>
 * <ul>
 *   <li>2.1 null 安全（toStringNullSafe）</li>
 *   <li>2.2 有值含字段（toStringWithValues）</li>
 * </ul>
 * <h2>equals/hashCode</h2>
 * <ul>
 *   <li>3.1 同值相等（equalsAndHashCode）</li>
 *   <li>3.2 同引用相等（equalsSameReference）</li>
 * </ul>
 * <h2>builder</h2>
 * <ul>
 *   <li>4.1 builder 构造（builder）</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.0
 */
public class CUpdateByTests {

    /**
     * 对应测试用例 1.1：无参构造：字段为 null
     */
    @Test
    public void noArgsConstructor() {

        CUpdateBy entity = new CUpdateBy();

        Assertions.assertNull(entity.getCreateById());
        Assertions.assertNull(entity.getCreateBy());
        Assertions.assertNull(entity.getUpdateById());
        Assertions.assertNull(entity.getUpdateBy());

    }

    /**
     * 对应测试用例 1.2：setter/getter：设置与读取
     */
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

    /**
     * 对应测试用例 2.1：null 安全
     */
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

    /**
     * 对应测试用例 2.2：有值含字段
     */
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

    /**
     * 对应测试用例 3.1：同值相等
     */
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

    /**
     * 对应测试用例 3.2：同引用相等
     */
    @Test
    public void equalsSameReference() {

        CUpdateBy a = new CUpdateBy();
        Assertions.assertEquals(a, a);

    }

    /**
     * 对应测试用例 4.1：builder 构造
     */
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
