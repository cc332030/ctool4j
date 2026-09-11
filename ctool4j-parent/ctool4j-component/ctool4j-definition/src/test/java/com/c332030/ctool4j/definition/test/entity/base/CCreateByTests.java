package com.c332030.ctool4j.definition.test.entity.base;

import com.c332030.ctool4j.definition.entity.base.CCreateBy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CCreateByTests
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
public class CCreateByTests {

    /**
     * 对应测试用例 1.1：无参构造：字段为 null
     */
    @Test
    public void noArgsConstructor() {

        CCreateBy entity = new CCreateBy();

        Assertions.assertNull(entity.getCreateById());
        Assertions.assertNull(entity.getCreateBy());

    }

    /**
     * 对应测试用例 1.2：setter/getter：设置与读取
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
     * 对应测试用例 2.1：null 安全
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
     * 对应测试用例 2.2：有值含字段
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
     * 对应测试用例 3.1：同值相等
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
     * 对应测试用例 3.2：同引用相等
     */
    @Test
    public void equalsSameReference() {

        CCreateBy a = new CCreateBy();
        Assertions.assertEquals(a, a);

    }

    /**
     * 对应测试用例 4.1：builder 构造
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
