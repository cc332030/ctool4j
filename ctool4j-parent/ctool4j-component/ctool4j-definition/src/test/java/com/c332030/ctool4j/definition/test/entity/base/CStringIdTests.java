package com.c332030.ctool4j.definition.test.entity.base;

import com.c332030.ctool4j.definition.entity.base.CStringId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CStringIdTests
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
public class CStringIdTests {

    /**
     * 对应测试用例 1.1：无参构造：字段为 null
     */
    @Test
    public void noArgsConstructor() {

        CStringId id = new CStringId();

        Assertions.assertNull(id.getId());

    }

    /**
     * 对应测试用例 1.2：setter/getter：设置与读取
     */
    @Test
    public void setterAndGetter() {

        CStringId id = new CStringId();
        id.setId("1");

        Assertions.assertEquals("1", id.getId());

    }

    /**
     * 对应测试用例 2.1：null 安全
     */
    @Test
    public void toStringNullSafe() {

        CStringId id = new CStringId();

        String str = id.toString();

        Assertions.assertNotNull(str);
        Assertions.assertTrue(str.contains("CStringId"));
        Assertions.assertTrue(str.contains("id=null"));

    }

    /**
     * 对应测试用例 2.2：有值含字段
     */
    @Test
    public void toStringWithValues() {

        CStringId id = new CStringId();
        id.setId("1");

        String str = id.toString();

        Assertions.assertTrue(str.contains("id=1"));

    }

    /**
     * 对应测试用例 3.1：同值相等
     */
    @Test
    public void equalsAndHashCode() {

        CStringId a = new CStringId();
        a.setId("1");

        CStringId b = new CStringId();
        b.setId("1");

        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());

        CStringId c = new CStringId();
        c.setId("2");

        Assertions.assertNotEquals(a, c);

    }

    /**
     * 对应测试用例 3.2：同引用相等
     */
    @Test
    public void equalsSameReference() {

        CStringId a = new CStringId();
        Assertions.assertEquals(a, a);

    }

    /**
     * 对应测试用例 4.1：builder 构造
     */
    @Test
    public void builder() {

        CStringId id = CStringId.builder()
            .id("5")
            .build();

        Assertions.assertEquals("5", id.getId());

    }

}
